#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform mat4 InvVP;
uniform mat4 PrevVP;
uniform vec3 CameraDelta;
uniform vec2 Resolution;
uniform float Strength;
uniform float Limit;
in vec2 uv;
out vec4 fragColor;
void main(){
    vec4 center=texture(Sampler0,uv);
    float depth=texture(Sampler1,uv).r;
    vec4 relative=InvVP*vec4(uv*2.-1.,depth*2.-1.,1.);
    relative/=max(abs(relative.w),.00001);
    vec4 old=PrevVP*vec4(relative.xyz+CameraDelta,1.);
    if(old.w<=0.){fragColor=center;return;}
    vec2 velocity=(uv-(old.xy/old.w*.5+.5))*Strength;
    float pixels=length(velocity*Resolution);
    velocity*=min(1.,Limit/max(pixels,.001));
    vec3 sum=center.rgb;float weight=1.;
    for(int i=1;i<8;i++){
        vec2 q=clamp(uv-velocity*(float(i)/7.-.45),vec2(.001),vec2(.999));
        float other=texture(Sampler1,q).r;
        // Reject samples across geometry boundaries instead of smearing silhouettes.
        float w=exp(-abs(other-depth)*180.);
        sum+=texture(Sampler0,q).rgb*w;weight+=w;
    }
    fragColor=vec4(sum/weight,1.);
}
