import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

public class AlgEx {
    private static class Tree {
        int x;
        int y;

        public Tree() {
            x = -1;
            y = -1;
        }

        public Tree(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class XPacked {
        Integer[] coordsX;
        Integer[] coordsXCounter;
        Map<Integer, Integer> oldIndexToNew;
        int[] nextIndex;
        int[] prevIndex;
        int length;

        public XPacked() {
            coordsX = null;
            coordsXCounter = null;
            oldIndexToNew = null;
            nextIndex = null;
            prevIndex = null;
            length = 0;
        }

        public XPacked(Tree[] trees, int lengthX) {
            LinkedList<Integer> coordsList = new LinkedList<>();
            LinkedList<Integer> counterList = new LinkedList<>();
            oldIndexToNew = new HashMap<>();
            Arrays.sort(trees, (a, b) -> a.x - b.x);
            coordsList.add(0);
            counterList.add(100000);
            for (int i = 0; i < trees.length; i++) {
                if (coordsList.peekLast() == trees[i].x) {
                    counterList.addLast(counterList.pollLast() + 1);
                } else {
                    coordsList.addLast(trees[i].x);
                    counterList.addLast(1);
                }
            }
            if (coordsList.peekLast() != lengthX) {
                coordsList.addLast(lengthX);
                counterList.addLast(100000);
            } else {
                counterList.pollLast();
                counterList.addLast(100000);
            }
            coordsX = new Integer[coordsList.size()];
            coordsXCounter = new Integer[coordsList.size()];
            coordsList.toArray(coordsX);
            counterList.toArray(coordsXCounter);
            length = coordsX.length;
            nextIndex = new int[length];
            prevIndex = new int[length];
            for (int i = 0; i < length; i++) {
                nextIndex[i] = i + 1;
                prevIndex[i] = i - 1;
                oldIndexToNew.put(coordsX[i], i);
            }
            nextIndex[length - 1] = -1;
            prevIndex[0] = -1;
        }
    }

    private static class YPacked {
        Integer[] coordsY;
        Integer[][] coordsX;
        int length;

        public YPacked() {
            coordsY = null;
            coordsX = null;
            length = 0;
        }

        public YPacked(Tree[] trees, int lengthX, int lengthY) {
            LinkedList<Integer> coordsList = new LinkedList<>();
            LinkedList<Set<Integer>> xList = new LinkedList<>();
            Set<Integer> tmp = new HashSet<>();
            Arrays.sort(trees, (a, b) -> a.y - b.y);
            coordsList.add(0);
            tmp.add(0);
            for (int i = 0; i < trees.length; i++) {
                if (trees[i].x != 0 && trees[i].x != lengthX) {
                    if (coordsList.peekLast() != trees[i].y) {
                        coordsList.addLast(trees[i].y);
                        xList.addLast(tmp);
                        tmp = new HashSet<>();
                        tmp.add(trees[i].x);
                    } else {
                        tmp.add(trees[i].x);
                    }
                }
            }
            if (tmp.size() != 0) {
                xList.addLast(tmp);
            }
            if (coordsList.peekLast() != lengthY) {
                coordsList.addLast(lengthY);
                tmp = new HashSet<>();
                tmp.add(lengthX);
                xList.addLast(tmp);
            }
            coordsY = new Integer[coordsList.size()];
            coordsX = new Integer[coordsList.size()][];
            coordsList.toArray(coordsY);
            length = coordsY.length;
            for (int i = 0; i < length; i++) {
                coordsX[i] = new Integer[xList.peekFirst().size()];
                xList.pollFirst().toArray(coordsX[i]);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
        PrintWriter writer = new PrintWriter("output.txt");
        String[] buf = reader.readLine().split(" ");
        Tree[] trees;
        XPacked xPacked;
        YPacked yPacked;
        Integer[] curCounter;
        int[] curNextIndex;
        int[] curPrevIndex;
        int treesNum = Integer.parseInt(buf[0]);
        int lengthX = Integer.parseInt(buf[1]);
        int lengthY = Integer.parseInt(buf[2]);
        int newX;
        int result = 0;
        trees = new Tree[treesNum];
        for (int i = 0; i < treesNum; i++) {
            buf = reader.readLine().split(" ");
            trees[i] = new Tree(Integer.parseInt(buf[0]), Integer.parseInt(buf[1]));
        }
        xPacked = new XPacked(trees, lengthX);
        yPacked = new YPacked(trees, lengthX, lengthY);
        for (int floor = 0; floor < yPacked.length; floor++) {
            for (int i = 0; i < yPacked.coordsX[floor].length; i++) {
                newX = xPacked.oldIndexToNew.get(yPacked.coordsX[floor][i]);
                if (xPacked.coordsXCounter[newX] > 1) {
                    xPacked.coordsXCounter[newX]--;
                } else {
                    xPacked.nextIndex[xPacked.prevIndex[newX]] = xPacked.nextIndex[newX];
                    xPacked.prevIndex[xPacked.nextIndex[newX]] = xPacked.prevIndex[newX];
                }
            }
            for (int i = 0; xPacked.nextIndex[i] != -1; i = xPacked.nextIndex[i]) {
                result = Math.max(result, (xPacked.coordsX[xPacked.nextIndex[i]] - xPacked.coordsX[i]) * (lengthY - yPacked.coordsY[floor]));
            }
            curCounter = xPacked.coordsXCounter.clone();
            curNextIndex = xPacked.nextIndex.clone();
            curPrevIndex = xPacked.prevIndex.clone();
            for (int ceiling = yPacked.length - 1; ceiling > floor; ceiling--) {
                for (int i = 0; i < yPacked.coordsX[ceiling].length; i++) {
                    newX = xPacked.oldIndexToNew.get(yPacked.coordsX[ceiling][i]);
                    if (curCounter[newX] > 1) {
                        curCounter[newX]--;
                    } else {
                        result = Math.max(result, (xPacked.coordsX[curNextIndex[newX]] -
                                xPacked.coordsX[curPrevIndex[newX]]) * (yPacked.coordsY[ceiling] - yPacked.coordsY[floor]));
                        curNextIndex[curPrevIndex[newX]] = curNextIndex[newX];
                        curPrevIndex[curNextIndex[newX]] = curPrevIndex[newX];
                    }
                }
            }
        }
        writer.print(result);
        writer.close();
        reader.close();
    }
}
