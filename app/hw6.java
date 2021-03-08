import java.util.*;

class Move
{
    public int f;
    public int o;
    public int t;

    public Move(int f, int o, int t)
    {
        this.f = f;
        this.o = o;
        this.t = t;
    }

    public Move reversed()
    { return new Move(t, o, f); }

    @Override
    public String toString()
    {
        return "(" + f + ", " + o + ", " + t + ")";
    }
}

class Board
{
    public int pegCount;
    public int[] cells;

    public Board(int emptyCell)
    {
        cells = new int[15];
        pegCount = 14;
        for (int i = 0; i < 15; i++)
            cells[i] = i == emptyCell ? 0 : 1;
    }

    public Board(int pegCount, int[] cells)
    {
        this.pegCount = pegCount;
        this.cells    = cells.clone();
    }

    public Board move(Move m)
    {
        if (cells[m.from] == 1 &&
                cells[m.over] == 1 &&
                cells[m.to]   == 0)
        {
            Board boardAfter = new Board(pegCount-1, cells.clone());
            boardAfter.cells[m.from] = 0;
            boardAfter.cells[m.over] = 0;
            boardAfter.cells[m.to]   = 1;

            return boardAfter;
        }

        return null;
    }
}

class StepIterator implements Iterator<Move>
{
    private Move[] moves;
    private Move   reversed;
    private int    i;

    public StepIterator(Move[] moves)
    {
        this.moves = moves;
        this.i     = 0;
    }

    @Override
    public boolean hasNext()
    { return i < moves.length || (i == moves.length && reversed != null); }

    @Override
    public Move next()
    {
        if (reversed != null)
        {
            Move result = reversed;
            reversed = null;
            return result;
        }

        Move m = moves[i++];
        reversed = m.reversed();

        return m;
    }
}

class StepList implements Iterable<Move>
{
    public static final Move[] moves =
            {
                    new Move(0, 1, 3),
                    new Move(0, 2, 5),
                    new Move(1, 3, 6),
                    new Move(1, 4, 8),
                    new Move(2, 4, 7),
                    new Move(2, 5, 9),
                    new Move(3, 6, 10),
                    new Move(3, 7, 12),
                    new Move(4, 7, 11),
                    new Move(4, 8, 13),
                    new Move(5, 8, 12),
                    new Move(5, 9, 14),
                    new Move(3, 4, 5),
                    new Move(6, 7, 8),
                    new Move(7, 8, 9),
                    new Move(10, 11, 12),
                    new Move(11, 12, 13),
                    new Move(12, 13, 14)
            };

    @Override
    public StepIterator iterator()
    { return new StepIterator(moves); }
}

public class Cracker
{
    static StepList steps()
    { return new StepList(); }

    static ArrayList<LinkedList<Move>> solve(Board b)
    {
        ArrayList<LinkedList<Move>> out = new ArrayList<LinkedList<Move>>();
        solve(b, out, 0);

        return out;
    }

    static LinkedList<Move> firstSolution(Board b)
    {
        ArrayList<LinkedList<Move>> out = new ArrayList<LinkedList<Move>>();
        solve(b, out, 1);

        if (out.size() == 0) // sanity
            return null;

        return out.get(0);
    }

    static void solve(Board b, ArrayList<LinkedList<Move>> solutions, int count)
    {
        if (b.pegCount == 1)
        {
            solutions.add(new LinkedList<Move>());
            return;
        }

        for (Move m : steps())
        {
            Board boardAfter = b.move(m);
            if (boardAfter == null) continue;

            ArrayList<LinkedList<Move>> tailSolutions = new ArrayList<LinkedList<Move>>();
            solve(boardAfter, tailSolutions, count);

            for (LinkedList<Move> solution : tailSolutions)
            {
                solution.add(0, m);
                solutions.add(solution);

                if (solutions.size() == count)
                    return;
            }
        }
    }

    static void printBoard(Board b)
    {
        System.out.print("(" + b.pegCount + ", [");
        for (int i = 0; i < b.cells.length; i++)
            System.out.print(i < b.cells.length-1 ? b.cells[i] + ", " : b.cells[i] + "])");
        System.out.println();
    }

    static void show(Board b)
    {
        int[][] lines = { {4,0,0}, {3,1,2}, {2,3,5}, {1,6,9}, {0,10,14} };
        for (int[] l : lines)
        {
            int spaces = l[0];
            int begin  = l[1];
            int end    = l[2];

            String space = new String();
            for (int i = 0; i < spaces; i++)
                space += " ";

            System.out.print(space);
            for (int i = begin; i <= end; i++)
                System.out.print(b.cells[i] == 0 ? ". " : "x ");

            System.out.println();
        }

        System.out.println();
    }

    static void replay(List<Move> moves, Board b)
    {
        show(b);
        for (Move m : moves)
        {
            b = b.move(m);
            show(b);
        }
    }

    static void terse()
    {
        for (int i = 0; i < 15; i++)
        {
            Board b = new Board(i);
            printBoard(b);
            List<Move> moves = firstSolution(b);
            for (Move m : moves)
            {
                System.out.println(m);
                b = b.move(m);
            }
            printBoard(b);
            System.out.println();
        }
    }

    static void go()
    {
        for (int i = 0; i < 5; i++)
        {
            System.out.println("=== " + i + " ===");
            Board b = new Board(i);
            replay(firstSolution(b), b);
            System.out.println();
        }
    }

    public static void main(String[] args)
    {
        go();
        terse();

        // This is how you can get all solutions for a particular board.

        //List<LinkedList<Move>> sols = solve(new Board(0));
        //System.out.println(sols.size());
    }
}
