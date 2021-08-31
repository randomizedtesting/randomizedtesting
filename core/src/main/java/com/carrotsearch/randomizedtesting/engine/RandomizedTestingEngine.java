package com.carrotsearch.randomizedtesting.engine;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;
import org.junit.platform.engine.support.hierarchical.Node;

public class RandomizedTestingEngine
    extends HierarchicalTestEngine<RandomizedTestingEngine.RandomizedContext> {

  private final RandomizedTestDescriptor engineDescriptor;

  public RandomizedTestingEngine() {
    this.engineDescriptor =
        new RandomizedTestDescriptor(
            UniqueId.forEngine(getId()), getId(), TestDescriptor.Type.CONTAINER);
  }

  @Override
  protected RandomizedContext createExecutionContext(ExecutionRequest executionRequest) {
    return new RandomizedContext();
  }

  @Override
  public String getId() {
    return "com.carrotsearch.randomizedtesting";
  }

  private static final EngineDiscoveryRequestResolver<TestDescriptor> resolver =
      EngineDiscoveryRequestResolver.builder()
          .addClassContainerSelectorResolver(
              clazz -> {
                System.out.println("class resolver: " + clazz.getName());
                return true;
              })
          .build();

  @Override
  public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
    engineDescriptor.addChild(
        new RandomizedTestDescriptor(
            uniqueId.append("seg", "test-foo"), "foo", TestDescriptor.Type.TEST));
    return engineDescriptor;
  }

  public static class RandomizedContext implements EngineExecutionContext {}

  private static class RandomizedTestDescriptor extends AbstractTestDescriptor
      implements Node<RandomizedTestingEngine.RandomizedContext> {

    private final Type type;

    protected RandomizedTestDescriptor(UniqueId uniqueId, String displayName, Type type) {
      super(uniqueId, displayName);
      this.type = type;
    }

    @Override
    public Type getType() {
      return type;
    }

    @Override
    public RandomizedContext execute(
        RandomizedContext context, DynamicTestExecutor dynamicTestExecutor) throws Exception {
      System.out.println("Execute: " + this.getUniqueId());
      return Node.super.execute(context, dynamicTestExecutor);
    }
  }
}
