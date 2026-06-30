#version 330

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 size;
uniform vec2 scrollOffset;
uniform vec2 scrollSize;
uniform float time;
uniform float zoom;

in vec2 texCoord0;
out vec4 fragColor;

float hash(vec2 p) { return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453); }
float noise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), f.x), f.y);
}
float fbm(vec2 p) {
    float v = 0.0, a = 0.5;
    for(int i = 0; i < 5; i++) { v += a * noise(p); p *= 2.0; a *= 0.5; }
    return v;
}

void main() {
    vec2 uv = texCoord0;
    vec2 aspect = vec2(size.x / size.y, 1.0);
    vec2 uvAspect = uv * aspect;
    vec2 center = aspect / 2.0;

    vec2 scrollPos = vec2(0.0);
    if(scrollSize.x > 0.0) scrollPos.x = scrollOffset.x / scrollSize.x;
    if(scrollSize.y > 0.0) scrollPos.y = scrollOffset.y / scrollSize.y;
    float zoomF = zoom / 16.0;
    vec2 vUV = (uvAspect - center) / zoomF + center;

    // Base dark
    vec3 col = vec3(0.012, 0.0, 0.018);

    // Fog
    vec2 fogUV = vUV * 1.0 + scrollPos * 0.03 + time * 0.015;
    float fog = fbm(fogUV + vec2(time * 0.03, -time * 0.05)) * 0.45 +
                fbm(fogUV * 1.5 - vec2(0.0, time * 0.08)) * 0.25;
    fog *= 0.75;
    vec3 fogColor = vec3(0.42, 0.05, 0.08);
    col += fogColor * fog * (1.0 - smoothstep(0.7, 2.2, length(vUV - center)));

    // Mist
    vec2 mistUV = vUV * 2.8 + scrollPos * 0.12 + time * 0.11;
    float mist = fbm(mistUV + vec2(time * 0.12, -time * 0.18)) * 0.35 +
                 fbm(mistUV * 3.2 - vec2(0.0, time * 0.22)) * 0.20;
    mist *= 0.55;
    vec3 mistColor = vec3(0.68, 0.12, 0.18);
    col += mistColor * mist * (1.0 - smoothstep(0.4, 1.4, length(vUV - center)));

    // Vignette
    float vignette = smoothstep(0.5, 1.6, length(vUV - center));
    col *= 1.0 - vignette * 0.60;
    
    // Subtle grain
    float grain = noise(uv * size * 0.01 + time) * 0.012;
    col += vec3(grain);

    fragColor = vec4(col, 1.0);
}