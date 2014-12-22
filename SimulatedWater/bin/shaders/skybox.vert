#version 150

in vec3 in_Position;

uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;
uniform mat4 viewMatrix;

out vec3 texCoord;

void main(void)
{
	gl_Position = projectionMatrix*viewMatrix*modelMatrix*vec4(in_Position, 1.0);
	texCoord = in_Position;
}
