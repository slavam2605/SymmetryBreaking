import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Моклев Вячеслав
 */
public class BruteForceChecker {

    public static final int BFS_ENUMERATED = 0;
    public static final int NOT_CONNECTED = 1;
    public static final int NOT_BFS_ENUMERATED = 2;

    public static int BFSEnumerated(int[][] y) {
        Queue<Integer> q = new ArrayDeque<>();
        boolean[] used = new boolean[y.length];
        int SYMBOLS_COUNT = y[0].length;
        q.add(0);
        used[0] = true;
        int order = 0;
        while (!q.isEmpty()) {
            int v = q.poll();
            if (v != order++) {
                return NOT_BFS_ENUMERATED;
            }
            for (int i = 0; i < SYMBOLS_COUNT; i++) {
                int u = y[v][i];
                if (!used[u]) {
                    q.add(u);
                    used[u] = true;
                }
            }
        }
        for (int i = 0; i < y.length; i++) {
            if (!used[i]) {
                return NOT_CONNECTED;
            }
        }
        return BFS_ENUMERATED;
    }

    public static void main(String[] args) {
        int[][] y = new int[3][2];
        int count = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    for (int l = 0; l < 3; l++) {
                        for (int q = 0; q < 3; q++) {
                            for (int w = 0; w < 3; w++) {
                                y[0][0] = i;
                                y[0][1] = j;
                                y[1][0] = k;
                                y[1][1] = l;
                                y[2][0] = q;
                                y[2][1] = w;
                                if (BFSEnumerated(y) == BFS_ENUMERATED) {
                                    count++;
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println(count);
    }
}
