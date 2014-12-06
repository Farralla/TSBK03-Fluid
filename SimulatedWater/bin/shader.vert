#version 150

in  vec3 in_Position;
in vec3 in_Color;

out vec3 pass_Color;

void main(void)
{
	pass_Color = 4*in_Color;
	gl_Position = vec4(in_Position, 1.0);
}
