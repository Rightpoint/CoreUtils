[![Raizlabs Repository](http://img.shields.io/badge/Raizlabs%20Repository-1.0.0-blue.svg?style=flat)](https://github.com/Raizlabs/maven-releases)

# CoreUtils

A core set of Android components that drastically improve the application
development process.

The main features of this library include:

1. Light-weight event-handling
2. JSON utilities
3. View-related helpers
4. Debug-related `Log` wrapper
5. Math-related utilities
6. Synchronization and threading helpers
7. Observable lists
8. Compatibility

## Including in your project

Add the maven repo url to your root build.gradle in the ```buildscript{}``` and ```allProjects{}``` blocks:

```groovy
  buildscript {
    repositories {
        maven { url "https://raw.github.com/Raizlabs/maven-releases/master/releases" }
    }
    dependencies {
      classpath 'com.raizlabs:Griddle:1.0.3'
    }
  }

  allprojects {
    repositories {
        maven { url "https://raw.github.com/Raizlabs/maven-releases/master/releases" }
    }
  }


```

Add the library to the project-level build.gradle, using the
[Griddle](https://github.com/Raizlabs/Griddle) plugin to simplify your build.gradle and link sources:

```groovy

  apply plugin: 'com.raizlabs.griddle'

  dependencies {
    mod "com.raizlabs.android:CoreUtils:1.0.0"
  }

```

## Usage

### Events

An `Event` is an arbitrary broadcast action that `Delegate` subscribe to. _Note_ by design, the broadcast happens on the same thread as where it's called.

For Thread-safe events, use the `HandlerEvent`. This class allows you to specify a custom `Handler` or use the default `Handler` to call the `Event` on. This is handy when you want to manipulate a `View` after an `Event` that occurs in a different thread.

For example, we want to know when a user turns on and off notifications and _perform_ an action when that occurs.

First define the `Event`


```java

public class MyClass {

  private static Event<Boolean> notificationChangedEvent = new Event<>();

  public static Event<Boolean> getNotificationChangedEvent() {
    return notificationChangedEvent;
  }

}


```

Register a listener where you want it called:

```java

private final Delegate<Boolean> notificationChangedDelegate = new Delegate<>() {

            @Override
            public void execute(Boolean notificationsOn) {

            }
        };

...
MyClass.getNotificationChangedEvent().addListener(notificationChangedDelegate);

```

*DONT* forget to unregister the listener when you don't need it anymore:

```java

MyClass.getNotificationChangedEvent().removeListener(notificationChangedDelegate);

```

Next where you change the value, call the `Event`:


```java

sharedPreferences.edit().putBoolean("NotificationsOn", false).commit();

MyClass.getNotificationChangedEvent().raiseEvent(false);


```

### Observable Lists

In java, there is no standard way to let a particular object know about a specific change to another object. This is why third-party implementations of the `Observer` pattern are popular among developers. This library provides a very simple `ListObserver` and an `ObservableList`

To listen to an `ObservableList`:

```java

ObservableList<Type> list = new ObservableListWrapper<>();
list.getListObserver().addListener(listObserverListener);


```

Perform any normal `List` action to trigger a callback on the `ListObserver`:

```java

list.remove(someObject);

list.set(0, someObject);

```

If you wish to bundle the notification into one large transaction, you can call:

```java

list.beginTransaction();

// modify the list completely

list.endTransaction();//notifies that the whole list has changed


```

## Maintainers

[dylanrjames](https://github.com/dylanrjames)

[agrosner](https://github.com/agrosner)
