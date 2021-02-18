package io.github.yangziwen.quickdao.core;

import java.util.function.Consumer;
import java.util.function.Function;

import io.github.yangziwen.quickdao.core.Order.Direction;
import io.github.yangziwen.quickdao.core.util.InvokedMethodExtractor;
import lombok.Getter;

public class TypedQuery<E> extends Query {

    private static final long serialVersionUID = 1L;

    private final Class<E> classType;

    private final InvokedMethodExtractor<E> extractor;

    @Getter
    private TypedCriteria<E> criteria;

    @Getter
    private TypedCriteria<E> havingCriteria;

    public TypedQuery(Class<E> classType) {
        this.classType = classType;
        this.extractor = new InvokedMethodExtractor<>(classType);
        this.criteria = new TypedCriteria<>(classType);
        this.havingCriteria = new TypedCriteria<>(classType, null, 0 + RepoKeys.HAVING);
        super.where(this.criteria);
        super.having(this.havingCriteria);
    }

    @Override
    public TypedQuery<E> select(String...fields) {
        super.select(fields);
        return this;
    }

    @Override
    public TypedQuery<E> select(Stmt stmt) {
        super.select(stmt);
        return this;
    }

    @Override
    public InnerQuery select(String field) {
        return this.new InnerQuery(new Stmt(field));
    }

    public TypedQuery<E> select(Function<E, ?> getter) {
        return this.select(extractor.extractFieldNameFromGetter(getter)).as("");
    }

    public InnerExprQuery selectExpr(Consumer<SqlFunctionExpression<E>> consumer) {
        SqlFunctionExpression<E> expression = new SqlFunctionExpression<>(classType);
        consumer.accept(expression);
        return this.new InnerExprQuery(new FunctionStmt<E>(expression, extractor));
    }

    public TypedQuery<E> where(TypedCriteria<E> criteria) {
        super.where(this.criteria = criteria);
        return this;
    }

    public TypedQuery<E> where(Consumer<TypedCriteria<E>> consumer) {
        if (this.criteria == null) {
            this.where(new TypedCriteria<>(classType));
        }
        consumer.accept(getCriteria());
        return this;
    }

    @Override
    public TypedQuery<E> groupBy(String stmt) {
        super.groupBy(stmt);
        return this;
    }

    public TypedQuery<E> groupBy(Function<E, ?> getter) {
        String name = extractor.extractFieldNameFromGetter(getter);
        return this.groupBy(name);
    }

    public TypedQuery<E> groupByKeyword(Function<E, ?> getter) {
        String name = extractor.extractFieldNameFromGetter(getter);
        return this.groupBy(name + ".keyword");
    }

    public TypedQuery<E> having(TypedCriteria<E> criteria) {
        criteria.setKey(RepoKeys.HAVING);
        super.having(this.havingCriteria = criteria);
        return this;
    }

    public TypedQuery<E> having(Consumer<TypedCriteria<E>> consumer) {
        if (this.havingCriteria == null) {
            this.having(new TypedCriteria<>(classType));
        }
        consumer.accept(getHavingCriteria());
        return this;
    }

    @Override
    public TypedQuery<E> orderBy(String name, Direction direction) {
        super.orderBy(name, direction);
        return this;
    }

    public TypedQuery<E> orderBy(Function<E, ?> getter, Direction direction) {
        String name = extractor.extractFieldNameFromGetter(getter);
        return this.orderBy(name, direction);
    }

    @Override
    public TypedQuery<E> orderBy(String name) {
        return this.orderBy(name, Direction.ASC);
    }

    public TypedQuery<E> orderBy(Function<E, ?> getter) {
        return this.orderBy(getter, Direction.ASC);
    }

    @Override
    public TypedQuery<E> offset(int offset) {
        super.offset(offset);
        return this;
    }

    @Override
    public TypedQuery<E> limit(int limit) {
        super.limit(limit);
        return this;
    }

    @Override
    public TypedQuery<E> forceIndex(String indexName) {
        super.forceIndex(indexName);
        return this;
    }

    public class InnerExprQuery extends Query.BaseQuery {

        public InnerExprQuery(Stmt stmt) {
            super(stmt);
        }

        @Override
        public TypedQuery<E> as(String alias) {
            this.stmt.setAlias(alias);
            return TypedQuery.this.select(this.stmt);
        }

        public TypedQuery<E> as(Function<E, ?> getter) {
            this.stmt.setAlias(extractor.extractFieldNameFromGetter(getter));
            return TypedQuery.this.select(this.stmt);
        }

    }

    public class InnerQuery extends Query.InnerQuery {

        public InnerQuery(Stmt stmt) {
            super(stmt);
        }

        @Override
        public TypedQuery<E> as(String alias) {
            this.stmt.setAlias(alias);
            return TypedQuery.this.select(this.stmt);
        }

        public TypedQuery<E> as(Function<E, ?> getter) {
            this.stmt.setAlias(extractor.extractFieldNameFromGetter(getter));
            return TypedQuery.this.select(this.stmt);
        }

        @Override
        public TypedQuery<E> select(String...fields) {
            return TypedQuery.this.select(this.stmt).select(fields);
        }

        @Override
        public InnerQuery select(String field) {
            return TypedQuery.this.select(this.stmt).select(field);
        }

        public TypedQuery<E> select(Function<E, ?> getter) {
            return TypedQuery.this.select(this.stmt).select(getter);
        }

        public InnerExprQuery selectExpr(Consumer<SqlFunctionExpression<E>> consumer) {
            return TypedQuery.this.select(this.stmt).selectExpr(consumer);
        }

        public TypedQuery<E> where(TypedCriteria<E> criteria) {
            return TypedQuery.this.select(this.stmt).where(criteria);
        }

        public TypedQuery<E> where(Consumer<TypedCriteria<E>> consumer) {
            return TypedQuery.this.select(this.stmt).where(consumer);
        }

        @Override
        public TypedQuery<E> groupBy(String stmt) {
            return TypedQuery.this.select(this.stmt).groupBy(stmt);
        }

        public TypedQuery<E> groupBy(Function<E, ?> getter) {
            return TypedQuery.this.select(this.stmt).groupBy(getter);
        }

        public TypedQuery<E> having(TypedCriteria<E> criteria) {
            return TypedQuery.this.select(this.stmt).having(criteria);
        }

        public TypedQuery<E> having(Consumer<TypedCriteria<E>> consumer) {
            return TypedQuery.this.select(this.stmt).having(consumer);
        }

        @Override
        public TypedQuery<E> orderBy(String name, Direction direction) {
            return TypedQuery.this.select(this.stmt).orderBy(name, direction);
        }

        public TypedQuery<E> orderBy(Function<E, ?> getter, Direction direction) {
            return TypedQuery.this.select(this.stmt).orderBy(getter, direction);
        }

        @Override
        public TypedQuery<E> orderBy(String name) {
            return TypedQuery.this.select(this.stmt).orderBy(name);
        }

        public TypedQuery<E> orderBy(Function<E, ?> getter) {
            return TypedQuery.this.select(this.stmt).orderBy(getter);
        }

        @Override
        public TypedQuery<E> offset(int offset) {
            return TypedQuery.this.select(this.stmt).offset(offset);
        }

        @Override
        public TypedQuery<E> limit(int limit) {
            return TypedQuery.this.select(this.stmt).limit(limit);
        }

    }

}
