#version 150
uniform sampler2D Sampler0;
uniform vec2 Direction;
in vec2 uv;
out vec4 fragColor;
void main(){
    vec3 c=texture(Sampler0,uv).rgb*.227027;
    c+=(texture(Sampler0,uv+Direction*1.384615).rgb+texture(Sampler0,uv-Direction*1.384615).rgb)*.316216;
    c+=(texture(Sampler0,uv+Direction*3.230769).rgb+texture(Sampler0,uv-Direction*3.230769).rgb)*.070270;
    fragColor=vec4(c,1.0);
}
