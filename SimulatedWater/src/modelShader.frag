#version 150

in vec3 ex_Normal;
in vec3 surf;

out vec4 out_Color;

uniform vec3 lightSourcesDirPosArr[2];
uniform vec3 lightSourcesColorArr[2];
uniform float specularExponent[2];

uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 modelMatrix;

void main(void)
{
	float transparency = 0.1;
	
	vec3 eyeDirection = vec3(normalize(-surf)); 
	vec3 reflectedLightDir, lightDir; 
	float diffuse, specularStrength, shadeR, shadeG, shadeB; 
 
	int i; 
	for(i=0 ; i<2 ; i++) 
	{ 
		lightDir = normalize(mat3(viewMatrix)*lightSourcesDirPosArr[i]); 
		diffuse = dot(ex_Normal,lightDir); 
		diffuse = max(0.0,diffuse); 
		clamp(diffuse,0,1); 
 
		reflectedLightDir = reflect(-lightDir,ex_Normal); 
		specularStrength = dot(reflectedLightDir,eyeDirection); 
		specularStrength = max(specularStrength,0.01); 
		specularStrength = pow(specularStrength,specularExponent[i]); 
		shadeR += (0.5*diffuse+0.6*specularStrength)*lightSourcesColorArr[i].x; 
		shadeG += (0.5*diffuse+0.6*specularStrength)*lightSourcesColorArr[i].y; 
		shadeB += (0.5*diffuse+0.6*specularStrength)*lightSourcesColorArr[i].z; 
	} 
	//vec3 color = vec3(0.01,0.05,0.85);
	//shadeR += color.x;
	//shadeG += color.y;
	//shadeB += color.z;
 
	shadeR = clamp(shadeR, 0, 1); 
	shadeG = clamp(shadeG, 0, 1); 
	shadeB = clamp(shadeB, 0, 1); 
 
	out_Color = vec4(shadeR, shadeG, shadeB, transparency);
}
