#version 150

in vec3 in_Position;
in vec3 in_Normal;

out vec3 ex_Normal; 
out vec3 surf; 

uniform mat4 modelMatrix; 
uniform mat4 projectionMatrix; 
uniform mat4 viewMatrix; 


void main(void)
{
	mat3 normalMatrix = mat3(viewMatrix*modelMatrix);

    	ex_Normal = normalize(normalMatrix*in_Normal); 
	surf = normalize(normalMatrix*in_Position); 

	gl_Position = projectionMatrix*viewMatrix*modelMatrix*vec4(in_Position, 1.0);
} 
