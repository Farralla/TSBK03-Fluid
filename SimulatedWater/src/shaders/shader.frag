#version 150

in vec3 pass_Color;

out vec4 outColor;

void main(void)
{
	outColor = vec4(pass_Color,0.1);
}
