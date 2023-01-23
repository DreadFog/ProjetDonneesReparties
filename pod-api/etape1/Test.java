import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Test {

    SharedObject intList;

    List<Integer> initializedList() {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < 5; i++) {
            list.add(20);
        }
        return list;
    }

    public Test() throws InterruptedException {

        SharedObject s = Client.lookup("Entiers");
        if (s == null) {
            s = Client.create(initializedList());
            Client.register("Entiers", s);
        }
        this.intList = s;
        // le serveur a un objet partagé de bien initialisé.
    }

    public static void main(String[] args) throws InterruptedException {

        Client.init();

        Test test = new Test();
        Random rand = new Random();
        Integer count = 0;
        while (count < 100000) {
            count++;
            test.intList.lock_write();

            List<Integer> list = (List<Integer>) test.intList.obj;

            Integer choice = rand.nextInt(list.size());
            list.set(choice, list.get(choice) + 1);

            choice = rand.nextInt(list.size());
            list.set(choice, list.get(choice) - 1);

            int somme = 0;
            for (Integer i : list) {
                somme += i;
            }
            if (somme != 100) {
                System.out.println("Somme incorrecte: " + somme);
                System.exit(1);
            }

            test.intList.unlock();
        }
        System.out.println("Test " + args[0] + " réussi");
    }

}
