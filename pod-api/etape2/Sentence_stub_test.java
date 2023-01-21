public class Sentence_stub_test extends SharedObject implements Sentence_itf {
public Sentence_stub_test(int param0){
	super(param0);
}

public void write(java.lang.String arg0)
{
	Sentence s = (Sentence) obj;
	s.write(arg0);
}

public String read()
{
	Sentence s = (Sentence) obj;
	return s.read();
}

}