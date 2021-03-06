// Do NOT add a package declaration here.

import edu.kit.iti.formal.lights.LightUpSolver;
import edu.kit.iti.formal.lights.Lights;
import edu.kit.iti.formal.lights.Solution;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

/**
 * This class provides a template for your own solution to the assignment.
 * <p>
 * The source code is added in the jar file.
 * Please consult it for detailed information.
 *
 * @author **Janek Speit**
 */
public class MyLightUpSolver extends LightUpSolver {
    public MyLightUpSolver(Lights lights) {
        super(lights);
    }

    @Override
    public Solution solve() throws ContradictionException, TimeoutException {

        try{
        addAllClauses(lights);
        }
        catch(ContradictionException e){
            return null;
        }

        int[] result = solver.findModel();
        placeLights(lights, result);

        if (result != null) {
            System.out.println("Model found!");
            return solution;
        } else {
            return null;
        }
    }

    private void addFieldClauses(int row, int column, Lights lights) throws ContradictionException{

        int dim = lights.getDimension();

        //Set of all fields in the specific row, bounded by walls or boarders.
        Set<int[]> rowSet = new HashSet<int[]>();
        rowSet.add(new int[]{row, column});

        //Set of all fields in the specific column, bounded by walls or boarders.
        Set<int[]> columnSet = new HashSet<int[]>();
        columnSet.add(new int[]{row, column});

        int c = column;
        int r = row;

        while((c > 0) && (lights.isEmpty(row, c-1))){
            rowSet.add(new int[]{row, c-1});
            c --;
        }
        c = column;
        while((c < (dim-1)) && (lights.isEmpty(row, c+1))){
            rowSet.add(new int[]{row, c+1});
            c ++;
        }
        c = column;
        while((r > 0) && (lights.isEmpty(r-1, column))){
            columnSet.add(new int[]{r-1, column});
            r --;
        }
        r=row;
        while((r < (dim-1)) && (lights.isEmpty(r+1, column))){
            columnSet.add(new int[]{r+1, column});
            r ++;
        }

        int i = 0;
        int[] row_lits = new int[rowSet.size()];
        for(int[] field: rowSet){
            row_lits[i] = field[0] * lights.getDimension() + field[1] + 1;
            i++;
        }

        solver.addAtMost(new VecInt(row_lits), 1);

        i = 0;
        int[] column_lits = new int[columnSet.size()];
        for(int[] field: columnSet){
            column_lits[i] = field[0] * lights.getDimension() + field[1] + 1;
            i++;
        }
        solver.addAtMost(new VecInt(column_lits), 1);

        //at least one field in the bounded row or column must have a light
        Set<int[]> properLightPositions = rowSet;
        properLightPositions.addAll(columnSet);

        int[] lits = new int[properLightPositions.size()];
        i = 0;
        for(int[] field: properLightPositions){
            lits[i] = field[0] * lights.getDimension() + field[1] + 1;
            i++;
        }
        addClause(lits);
    }


    private void addBlockConstraintClauses(int row, int column, Lights lights) throws ContradictionException {
        int wallConstraint = lights.getBlockConstraint(row, column);
        Set<int[]> neighborsSet = new HashSet<>();
        int dim = lights.getDimension();

        if((row)>0 && lights.isEmpty(row-1, column)){
                neighborsSet.add(new int[]{row-1, column});
        }

        if((row)<(dim-1) && lights.isEmpty(row+1, column)){
                neighborsSet.add(new int[]{row + 1, column});
        }

        if((column)<(dim-1) && lights.isEmpty(row, column+1)){
                neighborsSet.add(new int[]{row, column + 1});
        }

        if((column)>0 && lights.isEmpty(row, column-1)){
                neighborsSet.add(new int[]{row, column - 1});
        }

        int[] lits = new int[neighborsSet.size()];
        int i = 0;
        for(int[] n: neighborsSet){
            lits[i] = (n[0] * lights.getDimension() + n[1] + 1);
            i++;
        }
        solver.addExactly(new VecInt(lits), wallConstraint);
    }

    public void addAllClauses(Lights lights) throws ContradictionException{
        int dim = lights.getDimension();
        //iterate over all fields and add clauses for each field
        for(int column = 0; column < dim; column ++ ){
            for(int row = 0; row < dim; row ++){
                if(lights.isEmpty(row, column)){
                    addFieldClauses(row, column, lights);
                }
               else if (lights.isConstrainedBlock(row, column)){
                    addBlockConstraintClauses(row, column, lights);
                }
            }
        }
    }

    public void placeLights(Lights lights, int[] model){
        int dim = lights.getDimension();
        for(int lit: model){
            //if literal is positive, add lamp to the corresponding field
            if(lit > 0){
                int row = ((lit -1) / dim);
                int column = lit - (dim * row) - 1;
                try {
                    solution.addLamp(row, column);
                } catch(InvalidParameterException e){

                }
            }
        }
    }

    
    /**
     * Adds the given literals as one clause to the sat solver instance.
     *
     * @param c the literals
     */
    private void addClause(int... c) throws ContradictionException {
        // Debugging:
        // System.out.println(Arrays.toString(c));
            solver.addClause(new VecInt(c));
    }

    
}
