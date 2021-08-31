package com.carrotsearch.randomizedtesting.engine;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;
import org.junit.platform.engine.support.hierarchical.Node;

public class RandomizedTestingEngine implements TestEngine {
  public RandomizedTestingEngine() {}

  @Override
  public String getId() {
    return "com.carrotsearch.randomizedtesting";
  }

  @Override
  public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
    var engineDescriptor =
        new RandomizedTestDescriptor(
            UniqueId.forEngine(getId()), getId(), TestDescriptor.Type.CONTAINER);

    engineDescriptor.addChild(
        new RandomizedTestDescriptor(
            uniqueId.append("seg", "test-foo"), "foo", TestDescriptor.Type.TEST));

    return engineDescriptor;
  }

  @Override
  public void execute(ExecutionRequest request) {
    var listener = request.getEngineExecutionListener();
    process(listener, request.getRootTestDescriptor());
  }

  private void process(EngineExecutionListener listener, TestDescriptor node) {
    listener.executionStarted(node);
    if (node.isContainer()) {
      for (var v : node.getChildren()) {
        process(listener, v);
      }
    }
    listener.executionFinished(node, TestExecutionResult.successful());
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
