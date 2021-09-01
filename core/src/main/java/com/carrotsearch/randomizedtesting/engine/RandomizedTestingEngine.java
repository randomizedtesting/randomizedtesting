package com.carrotsearch.randomizedtesting.engine;

import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;
import org.junit.platform.engine.support.discovery.SelectorResolver;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;
import org.junit.platform.engine.support.hierarchical.Node;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toCollection;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;

public class RandomizedTestingEngine implements TestEngine {
  public RandomizedTestingEngine() {}

  @Override
  public String getId() {
    return "com.carrotsearch.randomizedtesting";
  }

  private static final EngineDiscoveryRequestResolver<TestDescriptor> resolver =
      EngineDiscoveryRequestResolver.builder()
          .addClassContainerSelectorResolver(
              candidate ->
                  ReflectionUtils.isPublic(candidate)
                      && !ReflectionUtils.isAbstract(candidate)
                      && !ReflectionUtils.isInnerClass(candidate))
          .addSelectorResolver(
              new SelectorResolver() {
                @Override
                public Resolution resolve(ClassSelector selector, Context context) {
                  var clazz = selector.getJavaClass();
                  if (new Predicates.IsTestClassWithTests().test(clazz)) {
                    var opt =
                        context.addToParent(
                            parent -> {
                              var CLASS_SEGMENT = "class";
                              var displayName = clazz.getName();
                              var classDescriptor =
                                  new RandomizedTestDescriptor(
                                      parent.getUniqueId().append(CLASS_SEGMENT, clazz.getName()),
                                      displayName,
                                      TestDescriptor.Type.CONTAINER);
                              return Optional.of(classDescriptor);
                            });

                    return opt.map(
                            descriptor ->
                                Resolution.match(
                                    Match.exact(descriptor, () -> methodSelectors(clazz))))
                        .orElse(Resolution.unresolved());
                  } else {
                    return Resolution.unresolved();
                  }
                }

                @Override
                public Resolution resolve(MethodSelector selector, Context context) {
                  var methodName = selector.getMethodName();

                  var opt =
                      context.addToParent(
                          parent -> {
                            RandomizedTestDescriptor method =
                                new RandomizedTestDescriptor(
                                    parent.getUniqueId().append("method", methodName),
                                    methodName,
                                    TestDescriptor.Type.TEST);
                            method.source = Optional.of(MethodSource.from(selector.getJavaMethod()));
                            return Optional.of(method);
                          });

                  return opt.map(desc -> Resolution.match(Match.exact(desc)))
                      .orElse(Resolution.unresolved());
                }

                private Set<? extends DiscoverySelector> methodSelectors(Class<?> testClass) {
                  var methods =
                      findMethods(testClass, new Predicates.IsTestableMethod(true)).stream()
                          .map(method -> DiscoverySelectors.selectMethod(testClass, method))
                          .collect(
                              toCollection((Supplier<Set<DiscoverySelector>>) LinkedHashSet::new));
                  return methods;
                }
              })
          .addSelectorResolver(new SelectorResolver() {})
          .build();

  @Override
  public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
    var engineDescriptor =
        new RandomizedTestDescriptor(
            UniqueId.forEngine(getId()), getId(), TestDescriptor.Type.CONTAINER);

    resolver.resolve(discoveryRequest, engineDescriptor);

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
    Optional<TestSource> source = Optional.empty();

    protected RandomizedTestDescriptor(UniqueId uniqueId, String displayName, Type type) {
      super(uniqueId, displayName);
      this.type = type;
    }

    @Override
    public Optional<TestSource> getSource() {
      return source;
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
