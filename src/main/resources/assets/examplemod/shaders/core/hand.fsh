#version 150
uniform sampler2D Sampler0;
uniform vec2 Resolution;
uniform vec3 Color;
uniform float Time;
uniform float Mode;
uniform float Opacity;
uniform float Radius;
in vec2 uv;
out vec4 fragColor;
void main(){
    vec4 base=texture(Sampler0,uv);float mask=base.a;float edge=0.;
    for(int i=0;i<12;i++){float a=float(i)*.523599;vec2 dir=vec2(cos(a),sin(a))/Resolution;
        edge+=texture(Sampler0,uv+dir*Radius).a*.045;
        edge+=texture(Sampler0,uv+dir*Radius*2.).a*.025;
    }
    float halo=max(0.,edge-mask);
    vec3 rgb=base.rgb;
    if(Mode>.5&&mask>.01){float wave=sin(uv.x*28.+sin(uv.y*21.+Time)*2.+Time*1.2)*.5+.5;
        vec3 fill=mix(Color*.35,Color+vec3(.25),wave);rgb=mix(rgb,fill,Opacity);
    }
    float alpha=max(mask,halo*Opacity);
    if(mask<.01)rgb=Color;
    else rgb+=Color*max(0.,1.-edge)*Opacity*.1;
    fragColor=vec4(rgb,alpha);
}
