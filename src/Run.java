import lejos.robotics.navigation.Waypoint;
import navigation.MyNavigator;

public class Run {

    private static final String LOG_TAG = Run.class.getSimpleName();

    public static void main(String[] args) {
        //TODO : Complete main function
        MyNavigator.goToDestination(new Waypoint(5, 5, 45), true);
    }
}
