package com.slava_110.logisimstuff.component.informatics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

/**
 * https://github.com/obsfx/kmap-solver-lib rewritten on Java
 */
public class KMapSolver {

    public static class Pos {
        public int row;
        public int col;

        public Pos(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public static class KMapGrayCode {
        public List<String> rows;
        public List<String> cols;

        public KMapGrayCode(List<String> rows, List<String> cols) {
            this.rows = rows;
            this.cols = cols;
        }
    }

    public static class KMapCell {
        public String binary;
        public int decimal;
        public int row;
        public int col;

        public KMapCell(String binary, int decimal, int row, int col) {
            this.binary = binary;
            this.decimal = decimal;
            this.row = row;
            this.col = col;
        }

        @Override
        public String toString() {
            return "KMapCell{" +
                    "binary='" + binary + '\'' +
                    ", decimal=" + decimal +
                    ", row=" + row +
                    ", col=" + col +
                    '}';
        }
    }

    public static class Region {
        public int w;
        public int h;

        public Region(int w, int h) {
            this.w = w;
            this.h = h;
        }
    }

    public static class KMapResult {
        public List<List<KMapCell>> groups;
        public String expression;

        public KMapResult(List<List<KMapCell>> groups, String expression) {
            this.groups = groups;
            this.expression = expression;
        }
    }

    public static Map<Integer, KMapGrayCode> KMapGrayCodes = new HashMap<Integer, KMapGrayCode>(){{
        put(2, new KMapGrayCode(
                Arrays.asList("0", "1"),
                Arrays.asList("0", "1")
        ));
        put(3, new KMapGrayCode(
                Arrays.asList("00", "01", "11", "10"),
                Arrays.asList("0", "1")
        ));
        put(4, new KMapGrayCode(
                Arrays.asList("00", "01", "11", "10"),
                Arrays.asList("00", "01", "11", "10")
        ));
    }};

    public static List<List<KMapCell>> getKMap(List<String> variables) {
        List<List<KMapCell>> kMap = new ArrayList<>();
        KMapGrayCode grayCodes = KMapGrayCodes.get(variables.size());

        if(grayCodes == null)
            return kMap;

        List<String> rows = grayCodes.rows;
        List<String> cols = grayCodes.cols;

        for (int row = 0; row < rows.size(); row++) {
            kMap.add(new ArrayList<>());

            for (int col = 0; col < cols.size(); col++) {
                String binary = rows.get(row) + cols.get(col);
                int decimal = Integer.parseInt(binary, 2);

                kMap.get(row).add(new KMapCell(binary, decimal, row, col));
            }
        }

        return kMap;
    }

    public static Pos findDecimalPos(int decimal, List<List<KMapCell>> kMap) {
        for (int row = 0; row < kMap.size(); row++) {
            for (int col = 0; col < kMap.get(0).size(); col++) {
                if(decimal == kMap.get(row).get(col).decimal)
                    return new Pos(row, col);
            }
        }

        return new Pos(0, 0);
    }

    public static List<Region> generateRegions(int rowCount, int colCount) {
        List<Region> regions = new ArrayList<>();

        for (int w = 1; w <= colCount; w *= 2) {
            for (int h = 1; h <= rowCount; h *= 2) {
                regions.add(new Region(w, h));

                if((w == 1 && h == 1) || (w == rowCount && h == colCount)) {
                    continue;
                }

                if (w == h) {
                    regions.add(new Region(-w, h));
                    regions.add(new Region(-w, -h));
                    regions.add(new Region(w, -h));
                    continue;
                }

                if (w > h) {
                    regions.add(new Region(-w, h));

                    if (h != 1) {
                        regions.add(new Region(w, -h));
                        regions.add(new Region(-w, -h));
                    }

                    continue;
                }

                if (w < h) {
                    regions.add(new Region(w, -h));

                    if (w != 1) {
                        regions.add(new Region(-w, h));
                        regions.add(new Region(-w, -h));
                    }

                    continue;
                }
            }
        }

        regions.sort((a, b) -> {
            int area_a = Math.abs(a.w * a.h);
            int area_b = Math.abs(b.w * b.h);

            return Integer.compare(area_a, area_b);
        });

        return regions;
    }

    public static List<KMapCell> group(int decimal, List<Integer> terms, List<List<KMapCell>> KMap, Queue<Integer> termQueue) {
        int rowCount = KMap.size();
        int colCount = KMap.get(0).size();

        Pos pos = findDecimalPos(decimal, KMap);
        int row = pos.row;
        int col = pos.col;

        List<Region> regions = generateRegions(rowCount, colCount);

        List<List<KMapCell>> composedGroups = new ArrayList<List<KMapCell>>(){{
            add(new ArrayList<KMapCell>(){{
                add(KMap.get(row).get(col));
            }});
        }};

        for (int i = 0; i < regions.size(); i++) {
            Region reg = regions.get(i);
            int w = reg.w;
            int h = reg.h;

            List<KMapCell> cells = checkRegion(w, h, KMap, row, rowCount, col, colCount, terms);

            if (cells == null)
                continue;

            if (cells.size() > composedGroups.get(0).size()) {
                composedGroups = new ArrayList<List<KMapCell>>() {{
                    add(cells);
                }};
                continue;
            }

            if (cells.size() == composedGroups.get(0).size()) {
                composedGroups.add(cells);
                continue;
            }
        }

        List<Integer> groupsThatContainCellsGrouppedBefore = new ArrayList<>();

        for (int i = 0; i < composedGroups.size(); i++) {
            List<KMapCell> cellsGrouppedBefore = composedGroups.get(i).stream()
                    .filter((cell) -> !termQueue.contains(cell.decimal))
                    .collect(Collectors.toList());

            if (cellsGrouppedBefore.size() > 0) {
                groupsThatContainCellsGrouppedBefore.add(i);
            }
        }

        if (groupsThatContainCellsGrouppedBefore.size() > 0 && groupsThatContainCellsGrouppedBefore.size() != composedGroups.size()) {
            return IntStream.range(0, composedGroups.size())
                    .filter(i -> !groupsThatContainCellsGrouppedBefore.contains(i))
                    .mapToObj(composedGroups::get)
                    .findFirst()
                    .orElse(null);
        }

        return composedGroups.get(0);
    }

    public static String extract(List<String> variables, List<KMapCell> group, boolean mknf) {
        String[] buffer = group.get(0).binary.split("");
        StringBuilder expressionBuilder = new StringBuilder(mknf && buffer.length > 1 ? "(" : "");

        for (int i = 1; i < group.size(); i++) {
            String[] binary = group.get(i).binary.split("");

            for (int j = 0; j < binary.length; j++) {
                if (!binary[j].equals(buffer[j]))
                    buffer[j] = "X";
            }
        }

        for (int i = 0; i < buffer.length; i++) {
            String value = buffer[i];

            if (!value.equals("X")) {
                expressionBuilder.append(variables.get(i));
                if(value.equals("0") ^ mknf)
                    expressionBuilder.append("'");
                if(mknf && i < buffer.length - 1)
                    expressionBuilder.append('+');
            }
        }

        if(expressionBuilder.charAt(expressionBuilder.length() - 1) == '+')
            expressionBuilder.deleteCharAt(expressionBuilder.length() - 1);

        if(mknf && buffer.length > 1)
            expressionBuilder.append(')');

        return expressionBuilder.toString();
    }

    public static KMapResult solve(List<String> variables, List<Integer> minterms, List<Integer> dontcares, boolean mknf) {
        List<List<KMapCell>> KMap = getKMap(variables);

        if(mknf) {
            List<Integer> finalMinterms = minterms;
            minterms = IntStream.range(0, (int) Math.pow(2, variables.size()))
                    .filter(num -> !finalMinterms.contains(num) && !dontcares.contains(num))
                    .boxed()
                    .collect(Collectors.toList());
        }

        List<List<KMapCell>> groups = new ArrayList<>();
        List<String> expressions = new ArrayList<>();

        Queue<Integer> termQueue = new LinkedList<>(minterms);

        while (termQueue.size() > 0) {
            int term = termQueue.peek();

            if (term < 0 || term > variables.size() * variables.size() - 1) {
                termQueue.poll();
                continue;
            }

            List<KMapCell> cells = group(term, minterms, KMap, termQueue);

            if (dontcares.size() > 0) {
                List<KMapCell> dc_cells = group(term, Stream.concat(minterms.stream(), dontcares.stream()).collect(Collectors.toList()), KMap, termQueue);
                if (dc_cells.size() > cells.size())
                    cells = dc_cells;
            }

            /*if(!cells.stream()
                    .filter(cell -> !groups.stream().anyMatch(group -> group.contains(cell)))
                    .findAny()
                    .isPresent()
            ) {
                continue;
            }*/

            groups.add(cells);

            List<KMapCell> finalCells = cells;
            termQueue = termQueue.stream()
                    .filter(_term -> !finalCells.stream().map(cell -> cell.decimal).anyMatch(cellD -> cellD == _term))
                    .collect(Collectors.toCollection(LinkedList::new));

            String expression = extract(variables, cells, mknf);
            expressions.add(expression);
        }

        // Fix excess groups

        ListIterator<List<KMapCell>> iter = groups.listIterator();
        ListIterator<String> expIter = expressions.listIterator();

        while (iter.hasNext()) {
            List<KMapCell> group = iter.next();
            if(!group.stream()
                    .filter(
                            cell -> !groups.stream()
                                    .filter(group1 -> group != group1)
                                    .anyMatch(
                                            group1 -> group1.contains(cell)
                                    )
                    )
                    .findAny()
                    .isPresent()
            ) {
                iter.remove();
                expIter.remove();
                continue;
            }

            expIter.next();
        }

        // End of dirty fix

        String total_expression = String.join(mknf ? "*" : "+", expressions);

        groups.forEach(group ->
                group.sort(
                        Comparator.comparing((KMapCell c) -> c.row)
                                .thenComparingInt(c -> c.col)
                )
        );

        return new KMapResult(
                groups,
                total_expression.isEmpty() ? "1" : total_expression
        );
    }

    @Nullable
    private static List<KMapCell> checkRegion(int w, int h, List<List<KMapCell>> KMap, int row, int rowCount, int col, int colCount, List<Integer> terms) {
        List<KMapCell> cells = new ArrayList<>();

        int r = 0;

        while (r != h) {
          int curRow = (row + r) % rowCount >= 0 ?
                    (row + r) % rowCount :
                    rowCount + ((row + r) % rowCount);

            int c = 0;

            while (c != w) {
                int curCol = (col + c) % colCount >= 0 ?
                        (col + c) % colCount :
                        colCount + ((col + c) % colCount);

                if (!terms.contains(KMap.get(curRow).get(curCol).decimal))
                    return null;

                cells.add(KMap.get(curRow).get(curCol));

                if(w < 0)
                    c--;
                else
                    c++;
            }

            if(h < 0)
                r--;
            else
                r++;
        }

        return cells;
    }
}
