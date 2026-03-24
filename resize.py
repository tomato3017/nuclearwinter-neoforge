import sys
from PIL import Image

input_path = '/home/tomato/.gemini/antigravity/brain/48a13da9-203e-44c2-8af2-625f5d4d1108/dead_grass_texture_5_1774324557482.png'
output_path = '/home/tomato/.gemini/antigravity/brain/48a13da9-203e-44c2-8af2-625f5d4d1108/dead_grass_texture_5_32x32.png'

img = Image.open(input_path)
img = img.resize((32, 32), Image.NEAREST)
img.save(output_path)
print(f"Resized image saved to {output_path}")
