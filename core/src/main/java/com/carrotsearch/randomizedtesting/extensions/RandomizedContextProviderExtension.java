package com.carrotsearch.randomizedtesting.extensions;

import com.carrotsearch.randomizedtesting.api.RandomizedContext;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class RandomizedContextProviderExtension
    implements TestInstanceFactory,
        TestInstancePreDestroyCallback,
        // InvocationInterceptor,
        ParameterResolver,
        BeforeAllCallback,
        BeforeEachCallback,
        AfterEachCallback,
        AfterAllCallback {
  private static final String KEY_EXECUTION_CONTEXT = ExtensionExecutionContext.class.getName();

  @Override
  public Object createTestInstance(
      TestInstanceFactoryContext tiCtx, ExtensionContext extensionContext)
      throws TestInstantiationException {
    Constructor<?> constructor = ReflectionUtils.getDeclaredConstructor(tiCtx.getTestClass());
    try {
      getExecutionContext(extensionContext).push(extensionContext);
      return constructor.newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new TestInstantiationException(
          "Could not instantiate class: " + tiCtx.getTestClass().getName(), e);
    }
  }

  @Override
  public void preDestroyTestInstance(ExtensionContext extensionContext) throws Exception {
    getExecutionContext(extensionContext).pop(extensionContext);
  }

  @Override
  public void beforeAll(ExtensionContext extensionContext) {
    var classStore = getClassExtensionStore(extensionContext);
    assert classStore.get(KEY_EXECUTION_CONTEXT) == null;

    var executionContext = new ExtensionExecutionContext(InitialSeed.compute());
    classStore.put(KEY_EXECUTION_CONTEXT, executionContext);

    executionContext.push(extensionContext);
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    var classStore = getClassExtensionStore(extensionContext);
    assert classStore.get(KEY_EXECUTION_CONTEXT) != null;
    var executionContext =
        classStore.remove(KEY_EXECUTION_CONTEXT, ExtensionExecutionContext.class);
    executionContext.pop(extensionContext);
  }

  @Override
  public void beforeEach(ExtensionContext extensionContext) {
    getExecutionContext(extensionContext).push(extensionContext);
  }

  @Override
  public void afterEach(ExtensionContext extensionContext) {
    getExecutionContext(extensionContext).pop(extensionContext);
  }

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType().equals(RandomizedContext.class)
        && getExecutionContext(extensionContext) != null;
  }

  @Override
  public RandomizedContext resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return getRandomizedContext(extensionContext);
  }

  private RandomizedContext getRandomizedContext(ExtensionContext extensionContext) {
    return getExecutionContext(extensionContext).getContext(extensionContext);
  }

  private ExtensionContext.Store getClassExtensionStore(ExtensionContext extensionContext) {
    return extensionContext.getStore(
        ExtensionContext.Namespace.create(
            RandomizedContextProviderExtension.class, extensionContext.getRequiredTestClass()));
  }

  private ExtensionExecutionContext getExecutionContext(ExtensionContext extensionContext) {
    var classStore = getClassExtensionStore(extensionContext);
    return classStore.get(KEY_EXECUTION_CONTEXT, ExtensionExecutionContext.class);
  }
}
