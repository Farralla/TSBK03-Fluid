#version 150

in vec3 ex_Normal;
in vec3 surf;

out vec4 out_Color;
uniform samplerCube cubeMap;

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main(void)
{	mat4 posMatrix = viewMatrix*modelMatrix;
	vec3 eyeDirection = normalize(vec3(posMatrix*vec4(surf,1.0))); //vec3(posMatrix[3][0],posMatrix[3][1],posMatrix[3][2]) -
	vec3 reflection = reflect(eyeDirection,normalize(ex_Normal));

	reflection = vec3(inverse(viewMatrix)*vec4(reflection, 0.0));
	vec4 color = vec4(1.1,1.1,2,0.6);
	out_Color =  color*texture(cubeMap,reflection);

	
	//float ratio = 1.0/1.3333; // air=1.0, water=1.3333
	//vec3 refraction = refract(eyeDirection, normalize(exNormal), ratio);
	//refraction = vec3(inverse(camMatrix)*vec4 (refraction.x,-refraction.y, refraction.z, 0.0));
	//outColor =  texture(cubeMap,refraction);
}
