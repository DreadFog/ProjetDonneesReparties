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
     * args[1] : Class destination
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
                    + " {\n");
            // constructors
            for (Constructor<?> c : classConstructors) { 
                List<Class<?>> type_param = Arrays.asList(c.getParameterTypes());
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
                fileWriter.write("){\n" + superCall + "}\n\n");
            }
            // methods
            for (Method m : classMethods) {
                System.out.println("Handling the method " + m);
                // The abstract methods are being implemented. Hence, the abstract keyword is removed.
                String modifier = Modifier.toString(m.getModifiers()).replace("abstract", "");
                String returnType = m.getReturnType().getSimpleName();
                String methodName = m.getName();
                
                String methodDeclaration = modifier + returnType + " " + methodName + "(";
                
                // Parameters
                List<String> parsedParameters = new ArrayList<String>();
                Parameter[] parameters = m.getParameters();
                for (Parameter p : parameters) {
                    parsedParameters.add(p.toString());
                }
                System.out.println("Parameters of the method: " + parsedParameters);
                String parametersForCall = "";
                String totalParameterString = "";
                for (String paramString : parsedParameters) {
                    if (parsedParameters.indexOf(paramString) == parsedParameters.size() - 1) {
                        totalParameterString = totalParameterString.concat(paramString);
                        parametersForCall = parametersForCall.concat(paramString.split(" ", 2)[1]);
                    } else {
                        totalParameterString = totalParameterString.concat(paramString.split(" ", 2)[1] + ", ");
                        parametersForCall.concat(paramString + ",");

                    }
                }
                methodDeclaration = methodDeclaration.concat(totalParameterString + ")");
                System.out.println("Method Declaration : " + methodDeclaration);

                fileWriter.write(methodDeclaration + "\n");

                // the third parameter is the object type that is going to be used.
                String objectType = args[2];
                // first instruction: casting the type of the object
                fileWriter.write("{\n\t" + objectType + " s = (" + objectType + ") obj;\n" );
                // second instruction
                if (returnType != "void"){ // 
                    fileWriter.write("\treturn ");
                } else {
                    fileWriter.write("\t");
                }
                fileWriter.write("s." + methodName + "(" + parametersForCall + ");\n");
                fileWriter.write("}\n\n");
            }
            
            fileWriter.write("}");
            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
