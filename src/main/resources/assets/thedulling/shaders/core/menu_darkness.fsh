#version 150

uniform vec2 ScreenSize;
uniform vec2 LightPos;
uniform float InnerRadius;
uniform float OuterRadius;
uniform float OverlayAlpha;

out vec4 fragColor;

void main() {
	float dist = distance(gl_FragCoord.xy, LightPos);

	float rt_brighten = 0.5;
	float rt_expand = 96.0;
	float rt_expandOuter = rt_expand + 32.0;

	// 0.0 inside the bright center, 1.0 out in the darkness.
	float darkness = smoothstep(InnerRadius + rt_expand, OuterRadius + rt_expand + rt_expandOuter, dist) - rt_brighten;

	fragColor = vec4(0.0, 0.0, 0.0, OverlayAlpha * darkness);
}