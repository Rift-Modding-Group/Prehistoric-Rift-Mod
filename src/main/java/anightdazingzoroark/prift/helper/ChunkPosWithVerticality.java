package anightdazingzoroark.prift.helper;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class ChunkPosWithVerticality extends ChunkPos {
    public final int y;

    public ChunkPosWithVerticality(int x, int y, int z) {
        super(x, z);
        this.y = y;
    }

    public ChunkPosWithVerticality(BlockPos pos) {
        super(pos);
        this.y = pos.getY() >> 4;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        else if (object == null) return false;
        else if (!(object instanceof ChunkPosWithVerticality)) return false;
        else {
            ChunkPosWithVerticality chunkPosWithVerticality = (ChunkPosWithVerticality) object;
            return this.x == chunkPosWithVerticality.x
                    && this.y == chunkPosWithVerticality.y
                    && this.z == chunkPosWithVerticality.z;
        }
    }

    public int getYStart() {
        return this.y << 4;
    }

    public int getYEnd() {
        return (this.y << 4) + 15;
    }

    @Override
    public String toString() {
        return "["+this.x+", "+this.y+", "+this.z+"]";
    }
}
