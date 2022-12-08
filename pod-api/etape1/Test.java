import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Test {
    
    SharedObject intList;

    List<Integer> initializedList() {
        List<Integer> list = new ArrayList<Integer>();
        for(int i = 0; i < 5; i++){
            list.add(20);
        }
        return list;
    }

    public Test() {
        
        SharedObject s = Client.lookup("Entiers");
        System.out.println(s);
		if (s == null) {
			s = Client.create(initializedList());
			Client.register("Entiers", s);
		}
        
        this.intList = s;
    }

    public static void main(String[] args) {
        
        Client.init();
        
        Test test = new Test();
        Random rand = new Random();
        while (true) {
            test.intList.lock_write();
            
            List<Integer> list = (List<Integer>)test.intList.obj;
            
            Integer choice = list.get(rand.nextInt(list.size()));
            choice = choice + 1;
            choice = list.get(rand.nextInt(list.size()));
            choice = choice - 1;
            
            int somme = 0;
            for (Integer i : list) {
                somme += i;
            }
            if (somme > 110 || somme < 90) {
                System.out.println("Somme incorrecte: " + somme);
                System.exit(1);
            }
            test.intList.unlock();
        }
    }

}
