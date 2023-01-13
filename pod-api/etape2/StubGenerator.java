import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.util.Arrays;
// command one liner
// rm -f Sentence_stub_test.java; javac -cp ../etape1 StubGenerator.java; java -cp ../etape1:. StubGenerator Sentence_itf Sentence_stub_test
public class StubGenerator {

    static Method[] classMethods;
    static FileWriter fileWriter;
    static Constructor<?>[] classConstructors;
    /**
     * args[0] : Interface source
     * args[1] : nom de la classe destination
     */
    public static void main(String[] args) {

        String sourceItf;
        String destClass;

        try {
            sourceItf = args[0];
            destClass = args[1];
            classMethods = Class.forName(sourceItf).getDeclaredMethods();
            classConstructors = Class.forName("SharedObject").getDeclaredConstructors();
            fileWriter = new FileWriter(destClass + ".java");
            // signature
            fileWriter.write("public class " + destClass + " extends SharedObject implements " + sourceItf
                    + ", java.io.Serializable {\n");
            // constructeur
            List<Class<?>> type_param = Arrays.asList(classConstructors[0].getParameterTypes());
            fileWriter.write("public " + destClass + "(");
            Integer argc = 0;
            String superCall = "\tsuper(";
            String currParam;
            for (Class<?> el: type_param) {
                currParam = "param" + argc;
                fileWriter.write(el.toString() + " " + currParam);
                superCall += currParam;
                argc++;
                if (type_param.indexOf(el) != type_param.size() - 1) {
                    fileWriter.write(", ");
                    superCall += ", ";
                }
                
            }
            superCall += ");\n";
            fileWriter.write("){\n" + superCall + "}\n");
            // méthodes
            for (Method m : classMethods) {
                System.out.println("Method : " + m);
                
                String modifier = Modifier.toString(m.getModifiers()).replace("abstract", "");
                String returnType = m.getReturnType().getSimpleName();
                String methodName = m.getName();
                
                String methodDeclaration = modifier + " " + returnType + " " + methodName + "(";
                System.out.println("Truc 1 : " + methodDeclaration);
                
                // Paramètres
                List<String> parameterStrings = new ArrayList<String>();
                Parameter[] parameters = m.getParameters();
                System.out.println("Parameters : " + parameters.toString());
                for (Parameter p : parameters) {
                    parameterStrings.add(p.toString());
                }
                
                String totalParameterString = "";
                for (String paramString : parameterStrings) {
                    if (parameterStrings.indexOf(paramString) == parameterStrings.size() - 1) {
                        System.out.println("Param String : " + paramString);
                        totalParameterString = totalParameterString.concat(paramString);
                    } else {
                        totalParameterString = totalParameterString.concat(paramString + ", ");
                    }
                }
                System.out.println("Paramètres : " + totalParameterString);

                methodDeclaration = methodDeclaration.concat(totalParameterString + ")");
                System.out.println("Method Declaration : " + methodDeclaration);

                fileWriter.write(methodDeclaration + "\n");

                // TODO : Corps de la méthode à générer
                // Besoin de prendre la classe Sentence en argument ?

            }
            
            fileWriter.write("}");
            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
