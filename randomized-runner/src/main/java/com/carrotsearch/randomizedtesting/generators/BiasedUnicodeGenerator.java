package com.carrotsearch.randomizedtesting.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A string generator that emits valid unicodeGenerator codepoints in accordance with given pseudo-character distribution.
 */
public class BiasedUnicodeGenerator extends StringGenerator {

  /** Unicode block start offset with its weight; weight should be between (0, 100] */
  public static class BlockStartAndWeight {
    private final int blockStart;
    private final int weight;
    public BlockStartAndWeight(int blockStart, int weight) {
      this.blockStart = blockStart;
      this.weight = weight;
    }
  }

  /** Cumulative distribution map */
  private static class CumulativeDistribution {
    private int[] givenBlockStartsIndices;
    private int[] cumDists;
    private int[] residualBlockStartsIndices;

    CumulativeDistribution(List<BlockStartAndWeight> distribution) {
      init(distribution);
    }

    void init(List<BlockStartAndWeight> distribution) {
      // calculate unicode blocks' cumulative distribution from given distribution map
      givenBlockStartsIndices = new int[distribution.size()];
      cumDists = new int[distribution.size()];
      int cumWeight = 0;
      for (int i = 0; i < distribution.size(); i++) {
        BlockStartAndWeight bw = distribution.get(i);
        int index = Arrays.binarySearch(blockStarts, bw.blockStart);
        assert index >= 0 : "invalid unicode block offset";
        givenBlockStartsIndices[i] = index;
        cumWeight += bw.weight;
        cumDists[i] = cumWeight;
      }
      assert cumWeight > 0 && cumWeight <= 100 : "invalid distribution";

      residualBlockStartsIndices = IntStream.range(0, blockStarts.length).filter(i -> {
        for (int idx : givenBlockStartsIndices) {
          if (i == idx) {
            return false;
          }
        }
        return true;
      }).toArray();
    }

    /** Select next block according to the cumulative distribution map */
    int getNextBlock(Random r) {
      int p = r.nextInt(100);
      // select a block from given unicode blocks
      for (int i = 0; i < cumDists.length; i++) {
        if (p <= cumDists[i]) {
          return givenBlockStartsIndices[i];
        }
      }
      // select a block from blocks that are not explicitly specified
      int i = r.nextInt(residualBlockStartsIndices.length);
      return residualBlockStartsIndices[i];
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("givenBlockStarts=[");
      for (int idx : givenBlockStartsIndices) {
        sb.append(String.format("0x%X", blockStarts[idx])); sb.append(",");
      }
      sb.append("]; ");
      sb.append("cumWeights=[");
      for (int w : cumDists) {
        sb.append(w); sb.append(",");
      }
      sb.append("]; ");
      sb.append("residualBlockStarts=[");
      for (int idx : residualBlockStartsIndices) {
        sb.append(String.format("0x%X", blockStarts[idx])); sb.append(",");
      }
      sb.append("]\n");
      return sb.toString();
    }

  }

  /** Index-aligned with {@link #blockEnds}. */
  private static final int[] blockStarts = {
    0x0000, 0x0080, 0x0100, 0x0180, 0x0250, 0x02B0, 0x0300, 0x0370, 0x0400, 
    0x0500, 0x0530, 0x0590, 0x0600, 0x0700, 0x0750, 0x0780, 0x07C0, 0x0800, 
    0x0900, 0x0980, 0x0A00, 0x0A80, 0x0B00, 0x0B80, 0x0C00, 0x0C80, 0x0D00, 
    0x0D80, 0x0E00, 0x0E80, 0x0F00, 0x1000, 0x10A0, 0x1100, 0x1200, 0x1380, 
    0x13A0, 0x1400, 0x1680, 0x16A0, 0x1700, 0x1720, 0x1740, 0x1760, 0x1780, 
    0x1800, 0x18B0, 0x1900, 0x1950, 0x1980, 0x19E0, 0x1A00, 0x1A20, 0x1B00, 
    0x1B80, 0x1C00, 0x1C50, 0x1CD0, 0x1D00, 0x1D80, 0x1DC0, 0x1E00, 0x1F00, 
    0x2000, 0x2070, 0x20A0, 0x20D0, 0x2100, 0x2150, 0x2190, 0x2200, 0x2300, 
    0x2400, 0x2440, 0x2460, 0x2500, 0x2580, 0x25A0, 0x2600, 0x2700, 0x27C0, 
    0x27F0, 0x2800, 0x2900, 0x2980, 0x2A00, 0x2B00, 0x2C00, 0x2C60, 0x2C80, 
    0x2D00, 0x2D30, 0x2D80, 0x2DE0, 0x2E00, 0x2E80, 0x2F00, 0x2FF0, 0x3000, 
    0x3040, 0x30A0, 0x3100, 0x3130, 0x3190, 0x31A0, 0x31C0, 0x31F0, 0x3200, 
    0x3300, 0x3400, 0x4DC0, 0x4E00, 0xA000, 0xA490, 0xA4D0, 0xA500, 0xA640, 
    0xA6A0, 0xA700, 0xA720, 0xA800, 0xA830, 0xA840, 0xA880, 0xA8E0, 0xA900, 
    0xA930, 0xA960, 0xA980, 0xAA00, 0xAA60, 0xAA80, 0xABC0, 0xAC00, 0xD7B0, 
    0xE000, 0xF900, 0xFB00, 0xFB50, 0xFE00, 0xFE10, 
    0xFE20, 0xFE30, 0xFE50, 0xFE70, 0xFF00, 0xFFF0, 
    0x10000, 0x10080, 0x10100, 0x10140, 0x10190, 0x101D0, 0x10280, 0x102A0, 
    0x10300, 0x10330, 0x10380, 0x103A0, 0x10400, 0x10450, 0x10480, 0x10800, 
    0x10840, 0x10900, 0x10920, 0x10A00, 0x10A60, 0x10B00, 0x10B40, 0x10B60, 
    0x10C00, 0x10E60, 0x11080, 0x12000, 0x12400, 0x13000, 0x1D000, 0x1D100, 
    0x1D200, 0x1D300, 0x1D360, 0x1D400, 0x1F000, 0x1F030, 0x1F100, 0x1F200, 
    0x20000, 0x2A700, 0x2F800, 0xE0000, 0xE0100, 0xF0000, 0x100000
  };

  /** Index-aligned with {@link #blockStarts}. */
  private static final int[] blockEnds = {
    0x007F, 0x00FF, 0x017F, 0x024F, 0x02AF, 0x02FF, 0x036F, 0x03FF, 0x04FF, 
    0x052F, 0x058F, 0x05FF, 0x06FF, 0x074F, 0x077F, 0x07BF, 0x07FF, 0x083F, 
    0x097F, 0x09FF, 0x0A7F, 0x0AFF, 0x0B7F, 0x0BFF, 0x0C7F, 0x0CFF, 0x0D7F, 
    0x0DFF, 0x0E7F, 0x0EFF, 0x0FFF, 0x109F, 0x10FF, 0x11FF, 0x137F, 0x139F, 
    0x13FF, 0x167F, 0x169F, 0x16FF, 0x171F, 0x173F, 0x175F, 0x177F, 0x17FF, 
    0x18AF, 0x18FF, 0x194F, 0x197F, 0x19DF, 0x19FF, 0x1A1F, 0x1AAF, 0x1B7F, 
    0x1BBF, 0x1C4F, 0x1C7F, 0x1CFF, 0x1D7F, 0x1DBF, 0x1DFF, 0x1EFF, 0x1FFF, 
    0x206F, 0x209F, 0x20CF, 0x20FF, 0x214F, 0x218F, 0x21FF, 0x22FF, 0x23FF, 
    0x243F, 0x245F, 0x24FF, 0x257F, 0x259F, 0x25FF, 0x26FF, 0x27BF, 0x27EF, 
    0x27FF, 0x28FF, 0x297F, 0x29FF, 0x2AFF, 0x2BFF, 0x2C5F, 0x2C7F, 0x2CFF, 
    0x2D2F, 0x2D7F, 0x2DDF, 0x2DFF, 0x2E7F, 0x2EFF, 0x2FDF, 0x2FFF, 0x303F, 
    0x309F, 0x30FF, 0x312F, 0x318F, 0x319F, 0x31BF, 0x31EF, 0x31FF, 0x32FF, 
    0x33FF, 0x4DBF, 0x4DFF, 0x9FFF, 0xA48F, 0xA4CF, 0xA4FF, 0xA63F, 0xA69F, 
    0xA6FF, 0xA71F, 0xA7FF, 0xA82F, 0xA83F, 0xA87F, 0xA8DF, 0xA8FF, 0xA92F, 
    0xA95F, 0xA97F, 0xA9DF, 0xAA5F, 0xAA7F, 0xAADF, 0xABFF, 0xD7AF, 0xD7FF, 
    0xF8FF, 0xFAFF, 0xFB4F, 0xFDFF, 0xFE0F, 0xFE1F, 
    0xFE2F, 0xFE4F, 0xFE6F, 0xFEFF, 0xFFEF, 0xFFFF, 
    0x1007F, 0x100FF, 0x1013F, 0x1018F, 0x101CF, 0x101FF, 0x1029F, 0x102DF, 
    0x1032F, 0x1034F, 0x1039F, 0x103DF, 0x1044F, 0x1047F, 0x104AF, 0x1083F, 
    0x1085F, 0x1091F, 0x1093F, 0x10A5F, 0x10A7F, 0x10B3F, 0x10B5F, 0x10B7F, 
    0x10C4F, 0x10E7F, 0x110CF, 0x123FF, 0x1247F, 0x1342F, 0x1D0FF, 0x1D1FF, 
    0x1D24F, 0x1D35F, 0x1D37F, 0x1D7FF, 0x1F02F, 0x1F09F, 0x1F1FF, 0x1F2FF, 
    0x2A6DF, 0x2B73F, 0x2FA1F, 0xE007F, 0xE01EF, 0xFFFFF, 0x10FFFF
  };

  private final CumulativeDistribution cumDistribution;

  /** Creates {@code BiasedUnicodeGenerator} with default distribution (fake-latin language) */
  public BiasedUnicodeGenerator() {
    List<BlockStartAndWeight> dist = new ArrayList<>();
    dist.add(new BlockStartAndWeight(0x0000, 75));
    dist.add(new BlockStartAndWeight(0x0080, 5));
    dist.add(new BlockStartAndWeight(0x0100, 5));
    dist.add(new BlockStartAndWeight(0x0180, 5));
    this.cumDistribution = new CumulativeDistribution(dist);
  }

  /** Creates {@code BiasedUnicodeGenerator} */
  public BiasedUnicodeGenerator(List<BlockStartAndWeight> distribution) {
    checkArgs(distribution);
    this.cumDistribution = new CumulativeDistribution(Collections.unmodifiableList(distribution));
  }

  private void checkArgs(List<BlockStartAndWeight> distribution) {
    int sumOfWeight = 0;
    for (BlockStartAndWeight bw : distribution) {
      if (Arrays.binarySearch(blockStarts, bw.blockStart) < 0) {
        throw new IllegalArgumentException("blockStart must be a valid unicode block range; got " + String.format("0x%X", bw.blockStart));
      }
      if (bw.weight <= 0 || bw.weight > 100) {
        throw new IllegalArgumentException("weight must be between (0, 100]; got " + bw.weight);
      }
      sumOfWeight += bw.weight;
    }
    if (sumOfWeight > 100) {
      throw new IllegalArgumentException("sum of weights must be equal to or less than 100; got " + sumOfWeight);
    }
    // check for duplicate
    Set<Integer> uniqBlocks = distribution.stream().map(bw -> bw.blockStart).collect(Collectors.toSet());
    if (uniqBlocks.size() < distribution.size()) {
      throw new IllegalArgumentException("blockStarts must be unique");
    }
  }

  @Override
  public String ofCodeUnitsLength(Random r, int minCodeUnits, int maxCodeUnits) {
    int length = RandomNumbers.randomIntBetween(r, minCodeUnits, maxCodeUnits);

    final StringBuilder sb = new StringBuilder();
    while (length > 0) {
      int block = cumDistribution.getNextBlock(r);
      int cp = RandomNumbers.randomIntBetween(r, blockStarts[block], blockEnds[block]);
      if (length > Character.charCount(cp)) {
        sb.appendCodePoint(cp);
        length -= Character.charCount(cp);
      } else {
        // Padding for blocks that don't fit.
        sb.appendCodePoint('a');
        length--;
      }
    }
    return sb.toString();
  }

  @Override
  public String ofCodePointsLength(Random r, int minCodePoints, int maxCodePoints) {
    final int length = RandomNumbers.randomIntBetween(r, minCodePoints, maxCodePoints);
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      int block = cumDistribution.getNextBlock(r);
      sb.appendCodePoint(RandomNumbers.randomIntBetween(r, blockStarts[block], blockEnds[block]));
    }
    return sb.toString();
  }

}