#version 150
uniform sampler2D Sampler0;
uniform vec4 Rect;
uniform vec2 Resolution;
uniform vec4 Tint;
uniform float Radius;
uniform float Mode;
uniform float Refraction;
uniform float Opacity;
out vec4 fragColor;
void main(){
    vec2 p=gl_FragCoord.xy-Rect.xy-Rect.zw*.5;
    float r=min(Radius,min(Rect.z,Rect.w)*.5);
    vec2 q=abs(p)-Rect.zw*.5+r;
    float d=length(max(q,0.0))+min(max(q.x,q.y),0.0)-r;
    float aa=max(fwidth(d),.65);
    float mask=1.0-smoothstep(-aa,aa,d);
    if(mask<.001)discard;
    vec3 color=Tint.rgb;float alpha=Tint.a;
    float edge=exp(-max(-d,0.0)/max(r*.6,1.0));
    vec2 normal=normalize(max(q,0.0)*sign(p)+vec2(.0001));
    if(q.x<0.0||q.y<0.0)normal=q.x>q.y?vec2(sign(p.x),0):vec2(0,sign(p.y));
    vec2 base=gl_FragCoord.xy/Resolution;
    if(Mode>.5){
        vec2 offset=Mode>1.5?normal*edge*Refraction/Resolution:vec2(0);
        vec2 uv=clamp(base-offset,vec2(.001),vec2(.999));
        vec3 bg=texture(Sampler0,uv).rgb;
        if(Mode>1.5){
            bg.r=texture(Sampler0,clamp(uv-offset*.09,vec2(.001),vec2(.999))).r;
            bg.b=texture(Sampler0,clamp(uv+offset*.09,vec2(.001),vec2(.999))).b;
        }
        color=mix(bg,Tint.rgb,clamp(Tint.a*.86,.18,.90));
        float light=max(dot(normal,normalize(vec2(-.55,.85))),0.0);
        float rim=pow(edge,3.2)*(.045+light*.19);
        color+=vec3(.70,.83,1.0)*rim*(Mode>1.5?1.0:.5);
        alpha=.97;
    }
    float top=clamp((p.y/Rect.w)+.5,0.,1.);
    color+=vec3(.16,.21,.30)*top*.09;
    float stroke=1.-smoothstep(0.,max(1.,aa),abs(d+1.));
    color=mix(color,vec3(.73,.83,1.),stroke*(.10+.12*max(normal.y,0.)));
    fragColor=vec4(color,alpha*mask*Opacity);
}
