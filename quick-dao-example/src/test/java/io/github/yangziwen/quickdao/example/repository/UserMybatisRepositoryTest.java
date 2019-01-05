package io.github.yangziwen.quickdao.example.repository;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

import io.github.yangziwen.quickdao.core.BaseRepository;
import io.github.yangziwen.quickdao.example.entity.User;

public class UserMybatisRepositoryTest extends BaseUserRepositoryTest {

    private static SqlSessionFactory sqlSessionFactory = createSqlSessionFactory(dataSource);

    private static SqlSessionFactory createSqlSessionFactory(DataSource dataSource) {
        TransactionFactory transactionFactory = new ManagedTransactionFactory();
        Environment environment = new Environment("test", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.setCacheEnabled(false);
        configuration.setLazyLoadingEnabled(false);
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setUseGeneratedKeys(true);
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    @Override
    protected BaseRepository<User> createRepository() {
        return new UserMybatisRepository(sqlSessionFactory.openSession());
    }

}
