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
        addAllClauses(lights);}
        catch(ContradictionException e){
            return null;
        }

        int[] result = solver.findModel();
        placeLights(lights, result);

        if (result != null) {
            System.out.println("Model found!");
            return solution;
        } else {//3b. unsatisfiable
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

        //only one light per bounded row
        for(int[] i: rowSet){
            for(int[] j: rowSet){
                int lit0 = -1*(i[0] * lights.getDimension() + i[1] + 1);
                int lit1 = -1*(j[0] * lights.getDimension() + j[1] + 1);
                if(lit0 != lit1) {
                    addClause(lit0, lit1);
                }
            }
        }

        //only one light per bounded column
        for(int[] i: columnSet){
            for(int[] j: columnSet){
                int lit0 = -1*(i[0] * lights.getDimension() + i[1] + 1);
                int lit1 = -1*(j[0] * lights.getDimension() + j[1] + 1);
                if(lit0 != lit1) {
                    addClause(lit0, lit1);
                }
            }
        }

        //at least one field in the bounded row or column must have a light
        Set<int[]> properLightPositions = rowSet;
        properLightPositions.addAll(columnSet); //Set of all possible light positions so that the specific field glows

        int[] lits = new int[properLightPositions.size()];
        int i = 0;
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

        //add upper neighbor
        if((row)>0 && lights.isEmpty(row-1, column)){
                neighborsSet.add(new int[]{row-1, column});
        }

        //add lower neighbor
        if((row)<(dim-1) && lights.isEmpty(row+1, column)){
                neighborsSet.add(new int[]{row + 1, column});
        }

        //add right neighbor
        if((column)<(dim-1) && lights.isEmpty(row, column+1)){
                neighborsSet.add(new int[]{row, column + 1});
        }

        //add left neighbor
        if((column)>0 && lights.isEmpty(row, column-1)){
                neighborsSet.add(new int[]{row, column - 1});
        }

        int numNeighbors = neighborsSet.size();

        if(wallConstraint > numNeighbors){
            //wall constraints not satisfiable => UNSAT
            System.out.println("riddle is UNSAT");
            addClause(-1);
            addClause(1);
        }

        if(wallConstraint == 0){
            //no neighbor must have a light
            for(int[] i: neighborsSet){
                int lit = -1*(i[0] * lights.getDimension() + i[1] + 1);
                addClause(lit);
            }
            return;
        }


        if(wallConstraint == 1){
            //not more than one neighbor must have a light
            for(int[] i: neighborsSet){
                for(int[] j: neighborsSet){
                    int lit1 = -1*(i[0] * lights.getDimension() + i[1] + 1);
                    int lit2 = -1*(j[0] * lights.getDimension() + j[1] + 1);
                    if(lit1 != lit2) {
                        addClause(lit1, lit2);
                    }
                }
            }

            //at least one field must have a light
            int i = 0;
            int[] lits = new int[numNeighbors];
            for(int[] n:neighborsSet){
                lits[i] = (n[0] * lights.getDimension() + n[1] + 1);
                i++;
            }
            addClause(lits);
            return;
        }

        if(wallConstraint == 2){
            switch(numNeighbors){
                //both neighbors must have a light
                case 2:
                    for(int n[]: neighborsSet){
                        int lit0 = (n[0] * lights.getDimension() + n[1] + 1);
                        addClause(lit0);
                    }
                    break;
                case 3:
                    //not more then 2 neighbors must have a light
                    for(int[] i: neighborsSet){
                        for(int[] j: neighborsSet){
                            for(int[] k: neighborsSet){
                                int lit0 = -1*(i[0] * lights.getDimension() + i[1] + 1);
                                int lit1 = -1*(j[0] * lights.getDimension() + j[1] + 1);
                                int lit2 = -1*(k[0] * lights.getDimension() + k[1] + 1);
                                if(lit0 != lit1 && lit1 != lit2 && lit0 != lit2){
                                    addClause(lit0, lit1, lit2);
                                }
                            }
                        }
                    }
                    //not less then 2 neighbors must have a light
                    for(int[] i: neighborsSet){
                        for(int[] j: neighborsSet){
                            int lit0 = (i[0] * lights.getDimension() + i[1] + 1);
                            int lit1 = (j[0] * lights.getDimension() + j[1] + 1);
                            if(lit0 != lit1){
                                addClause(lit0, lit1);
                            }
                        }
                    }
                    break;

                case 4:
                //not more then 2 neighbors must have a light
                for(int[] i: neighborsSet){
                    for(int[] j: neighborsSet){
                        for(int[] k: neighborsSet){
                            int lit0 = -1*(i[0] * lights.getDimension() + i[1] + 1);
                            int lit1 = -1*(j[0] * lights.getDimension() + j[1] + 1);
                            int lit2 = -1*(k[0] * lights.getDimension() + k[1] + 1);
                            if(lit0 != lit1 && lit1 != lit2 && lit0 != lit2){
                                addClause(lit0, lit1, lit2);
                            }
                        }
                    }
                }

                //not less then 2 neighbors must have a light
                for(int[] i: neighborsSet){
                    for(int[] j: neighborsSet){
                        for(int[] k: neighborsSet){
                            int lit0 = (i[0] * lights.getDimension() + i[1] + 1);
                            int lit1 = (j[0] * lights.getDimension() + j[1] + 1);
                            int lit2 = (k[0] * lights.getDimension() + k[1] + 1);
                            if(lit0 != lit1 && lit1 != lit2 && lit0 != lit2){
                                addClause(lit0, lit1, lit2);
                            }
                        }
                    }
                }
                break;
            }
            return;
        }

        if(wallConstraint == 3){
            switch(numNeighbors){
                case 3:
                    //all three neighbors must have a light
                    for(int n[]: neighborsSet){
                        int lit0 = (n[0] * lights.getDimension() + n[1] + 1);
                        addClause(lit0);
                    }
                    break;

                case 4:
                    //not all neighbors are allowed to have a light.
                    int[] lits = new int[4];
                    int i = 0;
                    for(int n[]: neighborsSet){
                        int lit0 = -1*(n[0] * lights.getDimension() + n[1] + 1);
                        lits[i] = lit0;
                        i++;
                    }
                    addClause(lits);

                    //more then 2 neighbors must have a light
                    for(int[] n: neighborsSet){
                        for(int[] m: neighborsSet){
                            int lit0 = (n[0] * lights.getDimension() + n[1] + 1);
                            int lit1 = (m[0] * lights.getDimension() + m[1] + 1);
                            if(lit0 != lit1){
                                addClause(lit0, lit1);
                            }
                        }
                    }
                    break;
            }
            return;
        }

        if(wallConstraint == 4){
            //all neighbors must have a light
            for(int n[]: neighborsSet){
                int lit = n[0] * lights.getDimension() + n[1] + 1;
                addClause(lit);
            }
            return;
        }
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
            //if variable is assigned true, add lamp to the corresponding field
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
