package com.slava_110.logisimstuff.component.informatics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Supposed to be unit test but... Currently used to test stuff manually. Might be changed in the future
 */
class KMapSolverTest {

    @Test
    void solveWeird() {
        solve(
                Arrays.asList("Q3", "Q2", "Q1", "Q0"),
                Arrays.asList(0,6,7,8), // 1,2,3,4,5
                Arrays.asList(9,10,11,12,13,14,15),
                true
        );
    }

    @Test
    void solve6() {
        solve(
                Arrays.asList("a", "b", "c", "d"),
                Arrays.asList(2, 5, 6, 7, 8, 9, 10, 11, 13, 14),
                Collections.emptyList(),
                false
        );
    }

    @Test
    void solve6_2() {
        solve(
                Arrays.asList("a", "b", "c", "d"),
                Arrays.asList(2, 5, 6, 7, 8, 9, 10, 11, 13, 14),
                Collections.emptyList(),
                true
        );
    }

    @Test
    void solve11_1() {
        solve(
                Arrays.asList("Q3", "Q2", "Q1", "Q0"),
                Arrays.asList(1),
                Arrays.asList(9, 10, 11, 12, 13, 14, 15),
                false
        );
    }

    @Test
    void solve11_2() {
        solve(
                Arrays.asList("Q3", "Q2", "Q1", "Q0"),
                Arrays.asList(0, 6, 7, 8),
                Arrays.asList(9, 10, 11, 12, 13, 14, 15),
                false
        );
    }

    @Test
    void solveProvided() {
        solve(
                Arrays.asList("x", "y", "z"),
                Arrays.asList(0, 1, 3, 4, 5, 6),
                Collections.emptyList(),
                false
        );
    }

    private void solve(List<String> variables, List<Integer> minterms, List<Integer> dontcares, boolean mknf) {
        KMapSolver.KMapResult res = KMapSolver.solve(variables, minterms, dontcares, mknf);
        System.out.println("Expression: " + res.expression);

        StringBuilder groupBuilder = new StringBuilder();

        for (List<KMapSolver.KMapCell> group : res.groups) {
            groupBuilder.append("Group: \n");
            for (KMapSolver.KMapCell cell : group) {
                groupBuilder.append(" Binary: ").append(cell.binary);
                groupBuilder.append(" Decimal: ").append(cell.decimal);
                groupBuilder.append(" Row: ").append(cell.row);
                groupBuilder.append(" Column: ").append(cell.col).append("\n");
            }
            groupBuilder.append("Group end\n");
        }

        System.out.println("Groups: \n" + groupBuilder);
    }
}