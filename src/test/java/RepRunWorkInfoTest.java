import org.junit.Test;

/**
 * @author WangShuai
 * @date 2024/4/9
 */
public class RepRunWorkInfoTest {

    @Test
    public void testRunWorkInfo(){


        String method = Thread.currentThread().getStackTrace()[1].getMethodName();

        System.out.println(method);


    }
}