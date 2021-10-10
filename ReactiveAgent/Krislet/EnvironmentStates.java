import java.lang.reflect.Field;

public class EnvironmentStates {
    String BallSeenByPlayer;
    String BallCloseToPlayer;
    String GoalVisibility;


    public EnvironmentStates(String BallSeenByPlayer, String BallCloseToPlayer, String GoalVisibility) {
        this.BallSeenByPlayer = BallSeenByPlayer;
        this.BallCloseToPlayer = BallCloseToPlayer;
        this.GoalVisibility = GoalVisibility;
    }

    //https://stackoverflow.com/questions/1526826/printing-all-variables-value-from-a-class
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append( this.getClass().getName() );
        result.append( " Object {" );
        result.append(newLine);

        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        //print field names paired with their values
        for ( Field field : fields  ) {
            result.append("  ");
            try {
                result.append( field.getName() );
                result.append(": ");
                //requires access to private field:
                result.append( field.get(this) );
            } catch ( IllegalAccessException ex ) {
                System.out.println(ex);
            }
            result.append(newLine);
        }
        result.append("}");

        return result.toString();
    }
}


