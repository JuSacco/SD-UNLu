import bpy
import random
bpy.data.scenes["Scene"].cycles.seed = random.randrange(0,650535)


bpy.ops.wm.save_as_mainfile()