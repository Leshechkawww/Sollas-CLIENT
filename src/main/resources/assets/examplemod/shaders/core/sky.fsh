#version 150
uniform mat4 InvVP;
uniform vec3 Color;
uniform float Time;
uniform float Mode;
in vec2 uv;
out vec4 fragColor;
float hash(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453);}
float phash(vec2 p,float period){return hash(vec2(mod(p.x,period),p.y));}
float pnoise(vec2 p,float period){vec2 i=floor(p),f=fract(p);f=f*f*(3.-2.*f);return mix(mix(phash(i,period),phash(i+vec2(1,0),period),f.x),mix(phash(i+vec2(0,1),period),phash(i+1.,period),f.x),f.y);}
void main(){
    vec4 p=InvVP*vec4(uv*2.-1.,1.,1.);vec3 ray=normalize(p.xyz);
    float horizon=smoothstep(-.05,.3,ray.y);vec2 sky=vec2(atan(ray.z,ray.x)*.159155+.5,asin(ray.y)*.63662);
    vec3 result=Color*.2;float alpha=.14*horizon;
    if(Mode>1.5){
        vec2 cell=floor(sky*vec2(600,300));vec2 point=fract(sky*vec2(600,300))-.5;
        float stars=step(.996,phash(cell,600.))*exp(-dot(point,point)*75.)*(.65+.35*sin(Time+phash(cell,600.)*30.));
        result=vec3(stars);alpha=max(stars,.14)*horizon;
        if(Mode<2.5){float band=exp(-pow((sky.y-.45-sin(sky.x*6.28318530718)*.2)*5.,2.));result+=Color*band*(pnoise(sky*35.,35.)*.7+.15);alpha=max(alpha,band*.55*horizon);}
        else if(Mode<3.5){float curtain=sin(sky.x*25.1327412287+Time*.15+pnoise(sky*8.,8.)*5.)*.13+.5;
            float band=exp(-abs(sky.y-curtain)*18.);float fold=pnoise(vec2(sky.x*100.,Time*.12),100.);result+=mix(vec3(.1,.9,.55),Color,fold)*band*(.3+fold);alpha=max(alpha,band*.65*horizon);}
        else {float phase=fract(Time*.22);vec2 head=vec2(fract(floor(Time*.22)*.618),.85-phase*.7);vec2 d=sky-head;d.x=mod(d.x+.5,1.)-.5;float streak=exp(-abs(d.y+d.x*.8)*230.)*step(0.,d.x)*exp(-d.x*45.);result+=vec3(.65,.8,1.)*streak;alpha=max(alpha,streak*horizon);}
    }
    fragColor=vec4(result,clamp(alpha,0.,.85));
}
