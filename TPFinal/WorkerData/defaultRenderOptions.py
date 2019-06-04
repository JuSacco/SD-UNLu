import bpy
#Sampling

bpy.data.scenes["Scene"].cycles.progressive = 'PATH'
bpy.data.scenes["Scene"].cycles.use_square_samples = 0
bpy.data.scenes["Scene"].cycles.samples = 15
bpy.data.scenes["Scene"].cycles.preview_samples = 0
bpy.data.scenes["Scene"].cycles.light_sampling_threshold = 0
bpy.data.scenes["Scene"].cycles.use_animated_seed = 1
bpy.data.scenes["Scene"].cycles.use_square_samples = 0
bpy.data.scenes["Scene"].cycles.sampling_pattern = 'CORRELATED_MUTI_JITTER'

#Disable Hair
bpy.data.scenes["Scene"].cycles_curves.use_curves = 0

#Light Paths
bpy.data.scenes["Scene"].cycles.max_bounces = 8
bpy.data.scenes["Scene"].cycles.diffuse_bounces = 0
bpy.data.scenes["Scene"].cycles.glossy_bounces = 2
bpy.data.scenes["Scene"].cycles.transparent_max_bounces = 8
bpy.data.scenes["Scene"].cycles.transmision_bounces = 8
bpy.data.scenes["Scene"].cycles.volume_bounces = 0

bpy.data.scenes["Scene"].cycles.sample_clamp_direct = 0
bpy.data.scenes["Scene"].cycles.sample_clamp_indirect = 3

bpy.data.scenes["Scene"].cycles.blur_glossy = 0.5
bpy.data.scenes["Scene"].cycles.caustics_reflective = 0
bpy.data.scenes["Scene"].cycles.caustics_refractive = 0

#Resolution and format
bpy.data.scenes["Scene"].render.resolution_x = 1920
bpy.data.scenes["Scene"].render.resolution_y = 1080
bpy.data.scenes["Scene"].render.resolution_percentage = 100

bpy.data.scenes["Scene"].frame_start = 1
bpy.data.scenes["Scene"].frame_end = 250

bpy.data.scenes["Scene"].render.filepath = 'C:\\Users\\sacco\\eclipse-workspace\\TPFinal/WorkerData//RenderedImages/'
bpy.data.scenes["Scene"].cycles_curves.use_overwrite = 0
bpy.data.scenes["Scene"].render.image_settings.color_mode = 'RGB'
bpy.data.scenes["Scene"].render.image_settings.color_depth = '16'
bpy.data.scenes["Scene"].render.image_settings.compression = 10

#Simplify
bpy.data.scenes["Scene"].render.use_simplify = 0

#Threads
bpy.data.scenes["Scene"].render.threads_mode = 'AUTO'

#World
bpy.data.worlds["World"].cycles.volume_sampling = 'MULTIPLE_IMPORTANCE'

bpy.ops.wm.save_as_mainfile()
