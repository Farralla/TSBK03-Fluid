#version 150
in vec3 in_Position;

//out vec4 pass_Color;

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;

void main(void)
{
	//projectionMatrix*viewMatrix*modelMatrix*
	gl_Position = projectionMatrix*viewMatrix*modelMatrix*vec4(in_Position, 1.0);
}
