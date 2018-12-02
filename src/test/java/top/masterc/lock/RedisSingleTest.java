package top.masterc.lock;

import top.masterc.constant.RedisToolsConstant;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;

import java.util.UUID;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 28/03/2018 23:35
 * @since JDK 1.8
 */
public class RedisSingleTest {

    private RedisLocker redisLocker;

    @Mock
    private JedisConnectionFactory jedisConnectionFactory ;

    @Mock
    private Jedis jedis;

    @Before
    public void setBefore() {
        MockitoAnnotations.initMocks(this);
        redisLocker = new RedisLocker.Builder(jedisConnectionFactory, RedisToolsConstant.SINGLE)
                .lockPrefix("lock_")
                .sleepTime(100)
                .build();



        RedisConnection redisConnection = new JedisConnection(jedis) ;
        Mockito.when(jedisConnectionFactory.getConnection()).thenReturn(redisConnection);

        jedis = (Jedis)redisConnection.getNativeConnection();


    }

    @Test
    public void unlock() throws Exception {

        Mockito.when(jedis.eval(Mockito.anyString(), Mockito.anyList(), Mockito.anyList())).thenReturn(1L) ;

        boolean locktest = redisLocker.unlock("test", "ec8ebca0-14ba0-4b23-99a8-b35fbba3629e");

        Assert.assertTrue(locktest);

        Mockito.verify(jedis).eval(Mockito.anyString(), Mockito.anyList(), Mockito.anyList());
    }


    @Test
    public void tryLock() throws Exception {

        String key = "test";
        String request = UUID.randomUUID().toString();
        Mockito.when(jedis.set(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn("OK");

        boolean locktest = redisLocker.tryLock(key, request);
        System.out.println("locktest=" + locktest);

        Assert.assertTrue(locktest);

        //check
        Mockito.verify(jedis).set(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt());
    }



    @Test
    public void lock() throws Exception {

        Mockito.when(jedis.set(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt())).thenReturn("OK");

        long start = System.currentTimeMillis();
        redisLocker.lock("test", UUID.randomUUID().toString());
        long end = System.currentTimeMillis();
        System.out.println("lock success expire=" + (end - start));

        Mockito.verify(jedis).set(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt());
    }




    @Test
    public void lock2() throws Exception {

        Mockito.when(jedis.set(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt())).thenReturn("OK");

        long start = System.currentTimeMillis();
        boolean lock = redisLocker.lock("test", UUID.randomUUID().toString(), 100);
        long end = System.currentTimeMillis();
        System.out.println("lock success expire=" + (end - start) + " lock = " + lock);

        Assert.assertTrue(lock);

        Mockito.verify(jedis).set(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt());

    }

    @Test
    public void lock2False() throws Exception {

        Mockito.when(jedis.set(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt())).thenReturn(null);

        long start = System.currentTimeMillis();
        boolean lock = redisLocker.lock("test", UUID.randomUUID().toString(), 100);
        long end = System.currentTimeMillis();
        System.out.println("lock success expire=" + (end - start) + " lock = " + lock);

        Assert.assertFalse(lock);

        //check was called 2 times
        Mockito.verify(jedis,Mockito.times(2)).set(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt());

    }



    @Test
    public void tryLock2() throws Exception {

        Mockito.when(jedis.set(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt())).thenReturn("OK");

        boolean lock = redisLocker.tryLock("test", UUID.randomUUID().toString(), 10 * 1000);

        Assert.assertTrue(lock);

        Mockito.verify(jedis).set(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void tryLock2False() throws Exception {

        Mockito.when(jedis.set(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt())).thenReturn(null);

        boolean lock = redisLocker.tryLock("test", UUID.randomUUID().toString(), 10 * 1000);

        Assert.assertFalse(lock);

        Mockito.verify(jedis).set(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt());
    }
}
