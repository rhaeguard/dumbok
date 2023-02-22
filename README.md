# dumbok

basically dumb lombok.

dumbok allows you to create type aliases in Java. Like this:

```java
@WithTypeAlias(
        alias = "Person",
        aliasFor = io.rhaeguard.demo.GenericPerson.class
)
public class Main {

    public static void main(String[] args) {
        Person person = new Person("Jane", 22);
        System.out.println(person.getName());
    }

}

// a class from another package
public class GenericPerson {

    private final String name;
    private final int age;

    public GenericPerson(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
```

It also allows you to have multiple type aliases:

```java
@WithTypeAlias(
        alias = "AliasA",
        aliasFor = io.rhaeguard.demo.ClassA.class
)
@WithTypeAlias(
        alias = "AliasB",
        aliasFor = io.rhaeguard.demo.ClassB.class
)
public class Main {}
```