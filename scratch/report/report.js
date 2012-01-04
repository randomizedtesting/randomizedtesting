var suites = [

{
  "slave": 0,
  "startTimestamp": 1322480032060,
  "executionTime": 44,
  "description": {
    "displayName": "com.carrotsearch.ant.tasks.junit4.tests.TestBeforeClassError",
    "className": "com.carrotsearch.ant.tasks.junit4.tests.TestBeforeClassError",
    "annotations": [],
    "children": [
      {
        "displayName": "method(com.carrotsearch.ant.tasks.junit4.tests.TestBeforeClassError)",
        "methodName": "method",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestBeforeClassError",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          }
        ],
        "children": []
      }
    ]
  },
  "tests": [],
  "failures": [
    {
      "throwableClass": "java.lang.RuntimeException",
      "throwableString": "java.lang.RuntimeException",
      "stackTrace": "java.lang.RuntimeException\n\tat com.carrotsearch.ant.tasks.junit4.tests.TestBeforeClassError.beforeClass(TestBeforeClassError.java:9)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n\tat java.lang.reflect.Method.invoke(Method.java:597)\n\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:45)\n\tat org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)\n\tat org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:42)\n\tat org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:27)\n\tat org.junit.runners.ParentRunner.run(ParentRunner.java:300)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:157)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:136)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.execute(SlaveMain.java:114)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.main(SlaveMain.java:182)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMainSafe.main(SlaveMainSafe.java:6)\n",
      "assertionViolation": false,
      "assumptionViolation": false,
      "errorViolation": true
    }
  ],
  "output": []
},

{
  "slave": 0,
  "startTimestamp": 1322480032126,
  "executionTime": 113,
  "description": {
    "displayName": "com.carrotsearch.ant.tasks.junit4.tests.TestStatuses",
    "className": "com.carrotsearch.ant.tasks.junit4.tests.TestStatuses",
    "annotations": [],
    "children": [
      {
        "displayName": "failure(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
        "methodName": "failure",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestStatuses",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          }
        ],
        "children": []
      },
      {
        "displayName": "error(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
        "methodName": "error",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestStatuses",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          }
        ],
        "children": []
      },
      {
        "displayName": "ok(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
        "methodName": "ok",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestStatuses",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          }
        ],
        "children": []
      },
      {
        "displayName": "ignored(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
        "methodName": "ignored",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestStatuses",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          },
          {
            "org.junit.Ignore": {
              "value": ""
            }
          }
        ],
        "children": []
      },
      {
        "displayName": "ignored_a(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
        "methodName": "ignored_a",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestStatuses",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          }
        ],
        "children": []
      }
    ]
  },
  "tests": [
    {
      "description": {
        "displayName": "ignored_a(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
        "methodName": "ignored_a",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestStatuses",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          }
        ],
        "children": []
      },
      "executionTime": 27,
      "slave": 0,
      "status": "IGNORED_ASSUMPTION",
      "wasFailure": false,
      "wasError": false,
      "wasIgnored": true,
      "wasIgnoredByAssumption": true,
      "failures": [
        {
          "throwableClass": "org.junit.internal.AssumptionViolatedException",
          "throwableString": "org.junit.internal.AssumptionViolatedException: got: \u003cfalse\u003e, expected: is \u003ctrue\u003e",
          "stackTrace": "org.junit.internal.AssumptionViolatedException: got: \u003cfalse\u003e, expected: is \u003ctrue\u003e\n\tat org.junit.Assume.assumeThat(Assume.java:70)\n\tat org.junit.Assume.assumeTrue(Assume.java:39)\n\tat com.carrotsearch.ant.tasks.junit4.tests.TestStatuses.ignored_a(TestStatuses.java:19)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n\tat java.lang.reflect.Method.invoke(Method.java:597)\n\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:45)\n\tat org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)\n\tat org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:42)\n\tat org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)\n\tat org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:263)\n\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:68)\n\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:47)\n\tat org.junit.runners.ParentRunner$3.run(ParentRunner.java:231)\n\tat org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:60)\n\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:229)\n\tat org.junit.runners.ParentRunner.access$000(ParentRunner.java:50)\n\tat org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:222)\n\tat org.junit.runners.ParentRunner.run(ParentRunner.java:300)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:157)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:136)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.execute(SlaveMain.java:114)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.main(SlaveMain.java:182)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMainSafe.main(SlaveMainSafe.java:6)\n",
          "assertionViolation": false,
          "assumptionViolation": true,
          "errorViolation": false
        }
      ]
    },
    {
      "description": {
        "displayName": "ignored(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
        "methodName": "ignored",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestStatuses",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          },
          {
            "org.junit.Ignore": {
              "value": ""
            }
          }
        ],
        "children": []
      },
      "executionTime": 0,
      "slave": 0,
      "status": "IGNORED",
      "wasFailure": false,
      "wasError": false,
      "wasIgnored": true,
      "wasIgnoredByAssumption": false,
      "failures": []
    },
    {
      "description": {
        "displayName": "ok(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
        "methodName": "ok",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestStatuses",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          }
        ],
        "children": []
      },
      "executionTime": 0,
      "slave": 0,
      "status": "OK",
      "wasFailure": false,
      "wasError": false,
      "wasIgnored": false,
      "wasIgnoredByAssumption": false,
      "failures": []
    },
    {
      "description": {
        "displayName": "error(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
        "methodName": "error",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestStatuses",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          }
        ],
        "children": []
      },
      "executionTime": 15,
      "slave": 0,
      "status": "ERROR",
      "wasFailure": false,
      "wasError": true,
      "wasIgnored": false,
      "wasIgnoredByAssumption": false,
      "failures": [
        {
          "throwableClass": "java.lang.RuntimeException",
          "throwableString": "java.lang.RuntimeException",
          "stackTrace": "java.lang.RuntimeException\n\tat com.carrotsearch.ant.tasks.junit4.tests.TestStatuses.error(TestStatuses.java:29)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n\tat java.lang.reflect.Method.invoke(Method.java:597)\n\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:45)\n\tat org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)\n\tat org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:42)\n\tat org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)\n\tat org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:263)\n\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:68)\n\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:47)\n\tat org.junit.runners.ParentRunner$3.run(ParentRunner.java:231)\n\tat org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:60)\n\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:229)\n\tat org.junit.runners.ParentRunner.access$000(ParentRunner.java:50)\n\tat org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:222)\n\tat org.junit.runners.ParentRunner.run(ParentRunner.java:300)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:157)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:136)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.execute(SlaveMain.java:114)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.main(SlaveMain.java:182)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMainSafe.main(SlaveMainSafe.java:6)\n",
          "assertionViolation": false,
          "assumptionViolation": false,
          "errorViolation": true
        }
      ]
    },
    {
      "description": {
        "displayName": "failure(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
        "methodName": "failure",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestStatuses",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          }
        ],
        "children": []
      },
      "executionTime": 17,
      "slave": 0,
      "status": "FAILURE",
      "wasFailure": true,
      "wasError": false,
      "wasIgnored": false,
      "wasIgnoredByAssumption": false,
      "failures": [
        {
          "throwableClass": "java.lang.AssertionError",
          "throwableString": "java.lang.AssertionError",
          "stackTrace": "java.lang.AssertionError\n\tat org.junit.Assert.fail(Assert.java:92)\n\tat org.junit.Assert.assertTrue(Assert.java:43)\n\tat org.junit.Assert.assertTrue(Assert.java:54)\n\tat com.carrotsearch.ant.tasks.junit4.tests.TestStatuses.failure(TestStatuses.java:24)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n\tat java.lang.reflect.Method.invoke(Method.java:597)\n\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:45)\n\tat org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)\n\tat org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:42)\n\tat org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)\n\tat org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:263)\n\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:68)\n\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:47)\n\tat org.junit.runners.ParentRunner$3.run(ParentRunner.java:231)\n\tat org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:60)\n\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:229)\n\tat org.junit.runners.ParentRunner.access$000(ParentRunner.java:50)\n\tat org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:222)\n\tat org.junit.runners.ParentRunner.run(ParentRunner.java:300)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:157)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:136)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.execute(SlaveMain.java:114)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.main(SlaveMain.java:182)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMainSafe.main(SlaveMainSafe.java:6)\n",
          "assertionViolation": true,
          "assumptionViolation": false,
          "errorViolation": false
        }
      ]
    }
  ],
  "failures": [],
  "output": [
    {
      "test": "failure(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
      "event": "TEST_STARTED"
    },
    {
      "test": "failure(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
      "event": "TEST_FINISHED"
    },
    {
      "test": "error(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
      "event": "TEST_STARTED"
    },
    {
      "test": "error(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
      "event": "TEST_FINISHED"
    },
    {
      "test": "ok(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
      "event": "TEST_STARTED"
    },
    {
      "test": "ok(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
      "event": "TEST_FINISHED"
    },
    {
      "test": "ignored_a(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
      "event": "TEST_STARTED"
    },
    {
      "test": "ignored_a(com.carrotsearch.ant.tasks.junit4.tests.TestStatuses)",
      "event": "TEST_FINISHED"
    }
  ]
},

{
  "slave": 0,
  "startTimestamp": 1322480032240,
  "executionTime": 0,
  "description": {
    "displayName": "com.carrotsearch.ant.tasks.junit4.tests.TestIgnoredSuite",
    "className": "com.carrotsearch.ant.tasks.junit4.tests.TestIgnoredSuite",
    "annotations": [
      {
        "org.junit.Ignore": {
          "value": ""
        }
      }
    ],
    "children": []
  },
  "tests": [],
  "failures": [],
  "output": []
},

{
  "slave": 0,
  "startTimestamp": 1322480032241,
  "executionTime": 3,
  "description": {
    "displayName": "com.carrotsearch.ant.tasks.junit4.tests.TestSysstreams",
    "className": "com.carrotsearch.ant.tasks.junit4.tests.TestSysstreams",
    "annotations": [],
    "children": [
      {
        "displayName": "ok(com.carrotsearch.ant.tasks.junit4.tests.TestSysstreams)",
        "methodName": "ok",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestSysstreams",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          }
        ],
        "children": []
      },
      {
        "displayName": "ok_sysout_syserr(com.carrotsearch.ant.tasks.junit4.tests.TestSysstreams)",
        "methodName": "ok_sysout_syserr",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestSysstreams",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          }
        ],
        "children": []
      }
    ]
  },
  "tests": [
    {
      "description": {
        "displayName": "ok_sysout_syserr(com.carrotsearch.ant.tasks.junit4.tests.TestSysstreams)",
        "methodName": "ok_sysout_syserr",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestSysstreams",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          }
        ],
        "children": []
      },
      "executionTime": 2,
      "slave": 0,
      "status": "OK",
      "wasFailure": false,
      "wasError": false,
      "wasIgnored": false,
      "wasIgnoredByAssumption": false,
      "failures": []
    },
    {
      "description": {
        "displayName": "ok(com.carrotsearch.ant.tasks.junit4.tests.TestSysstreams)",
        "methodName": "ok",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestSysstreams",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          }
        ],
        "children": []
      },
      "executionTime": 0,
      "slave": 0,
      "status": "OK",
      "wasFailure": false,
      "wasError": false,
      "wasIgnored": false,
      "wasIgnoredByAssumption": false,
      "failures": []
    }
  ],
  "failures": [],
  "output": [
    {
      "test": "ok(com.carrotsearch.ant.tasks.junit4.tests.TestSysstreams)",
      "event": "TEST_STARTED"
    },
    {
      "test": "ok(com.carrotsearch.ant.tasks.junit4.tests.TestSysstreams)",
      "event": "TEST_FINISHED"
    },
    {
      "test": "ok_sysout_syserr(com.carrotsearch.ant.tasks.junit4.tests.TestSysstreams)",
      "event": "TEST_STARTED"
    },
    {
      "out": "sysout"
    },
    {
      "err": "syserr"
    },
    {
      "out": "-sysout-contd."
    },
    {
      "err": "-syserr-contd."
    },
    {
      "test": "ok_sysout_syserr(com.carrotsearch.ant.tasks.junit4.tests.TestSysstreams)",
      "event": "TEST_FINISHED"
    }
  ]
},

{
  "slave": 0,
  "startTimestamp": 1322480032271,
  "executionTime": 14,
  "description": {
    "displayName": "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription",
    "className": "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription",
    "annotations": [
      {
        "org.junit.runner.RunWith": {
          "value": "org.junit.runners.Suite"
        }
      },
      {
        "org.junit.runners.Suite$SuiteClasses": {
          "value": [
            "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub1",
            "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub2",
            "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub3"
          ]
        }
      }
    ],
    "children": [
      {
        "displayName": "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub1",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub1",
        "annotations": [],
        "children": [
          {
            "displayName": "method1(com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub1)",
            "methodName": "method1",
            "className": "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub1",
            "annotations": [
              {
                "org.junit.Test": {
                  "timeout": 0,
                  "expected": "org.junit.Test$None"
                }
              }
            ],
            "children": []
          }
        ]
      },
      {
        "displayName": "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub2",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub2",
        "annotations": [],
        "children": [
          {
            "displayName": "method1(com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub2)",
            "methodName": "method1",
            "className": "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub2",
            "annotations": [
              {
                "org.junit.Test": {
                  "timeout": 0,
                  "expected": "org.junit.Test$None"
                }
              }
            ],
            "children": []
          }
        ]
      },
      {
        "displayName": "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub3",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub3",
        "annotations": [],
        "children": [
          {
            "displayName": "method1(com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub3)",
            "methodName": "method1",
            "className": "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub3",
            "annotations": [
              {
                "org.junit.Test": {
                  "timeout": 0,
                  "expected": "org.junit.Test$None"
                }
              }
            ],
            "children": []
          }
        ]
      }
    ]
  },
  "tests": [
    {
      "description": {
        "displayName": "method1(com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub3)",
        "methodName": "method1",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub3",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          }
        ],
        "children": []
      },
      "executionTime": 2,
      "slave": 0,
      "status": "ERROR",
      "wasFailure": false,
      "wasError": true,
      "wasIgnored": false,
      "wasIgnoredByAssumption": false,
      "failures": [
        {
          "throwableClass": "java.lang.RuntimeException",
          "throwableString": "java.lang.RuntimeException",
          "stackTrace": "java.lang.RuntimeException\n\tat com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub3.method1(TestHierarchicalSuiteDescription.java:40)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n\tat java.lang.reflect.Method.invoke(Method.java:597)\n\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:45)\n\tat org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)\n\tat org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:42)\n\tat org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)\n\tat org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:263)\n\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:68)\n\tat org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:47)\n\tat org.junit.runners.ParentRunner$3.run(ParentRunner.java:231)\n\tat org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:60)\n\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:229)\n\tat org.junit.runners.ParentRunner.access$000(ParentRunner.java:50)\n\tat org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:222)\n\tat org.junit.runners.ParentRunner.run(ParentRunner.java:300)\n\tat org.junit.runners.Suite.runChild(Suite.java:128)\n\tat org.junit.runners.Suite.runChild(Suite.java:24)\n\tat org.junit.runners.ParentRunner$3.run(ParentRunner.java:231)\n\tat org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:60)\n\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:229)\n\tat org.junit.runners.ParentRunner.access$000(ParentRunner.java:50)\n\tat org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:222)\n\tat org.junit.internal.runners.statements.RunAfters.evaluate(RunAfters.java:30)\n\tat org.junit.runners.ParentRunner.run(ParentRunner.java:300)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:157)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:136)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.execute(SlaveMain.java:114)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.main(SlaveMain.java:182)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMainSafe.main(SlaveMainSafe.java:6)\n",
          "assertionViolation": false,
          "assumptionViolation": false,
          "errorViolation": true
        }
      ]
    },
    {
      "description": {
        "displayName": "method1(com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub2)",
        "methodName": "method1",
        "className": "com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub2",
        "annotations": [
          {
            "org.junit.Test": {
              "timeout": 0,
              "expected": "org.junit.Test$None"
            }
          }
        ],
        "children": []
      },
      "executionTime": 0,
      "slave": 0,
      "status": "OK",
      "wasFailure": false,
      "wasError": false,
      "wasIgnored": false,
      "wasIgnoredByAssumption": false,
      "failures": []
    }
  ],
  "failures": [
    {
      "throwableClass": "java.lang.RuntimeException",
      "throwableString": "java.lang.RuntimeException",
      "stackTrace": "java.lang.RuntimeException\n\tat com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub1.beforeClass(TestHierarchicalSuiteDescription.java:23)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n\tat java.lang.reflect.Method.invoke(Method.java:597)\n\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:45)\n\tat org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)\n\tat org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:42)\n\tat org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:27)\n\tat org.junit.runners.ParentRunner.run(ParentRunner.java:300)\n\tat org.junit.runners.Suite.runChild(Suite.java:128)\n\tat org.junit.runners.Suite.runChild(Suite.java:24)\n\tat org.junit.runners.ParentRunner$3.run(ParentRunner.java:231)\n\tat org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:60)\n\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:229)\n\tat org.junit.runners.ParentRunner.access$000(ParentRunner.java:50)\n\tat org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:222)\n\tat org.junit.internal.runners.statements.RunAfters.evaluate(RunAfters.java:30)\n\tat org.junit.runners.ParentRunner.run(ParentRunner.java:300)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:157)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:136)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.execute(SlaveMain.java:114)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.main(SlaveMain.java:182)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMainSafe.main(SlaveMainSafe.java:6)\n",
      "assertionViolation": false,
      "assumptionViolation": false,
      "errorViolation": true
    },
    {
      "throwableClass": "java.lang.RuntimeException",
      "throwableString": "java.lang.RuntimeException",
      "stackTrace": "java.lang.RuntimeException\n\tat com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub2.afterClass(TestHierarchicalSuiteDescription.java:33)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n\tat java.lang.reflect.Method.invoke(Method.java:597)\n\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:45)\n\tat org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)\n\tat org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:42)\n\tat org.junit.internal.runners.statements.RunAfters.evaluate(RunAfters.java:36)\n\tat org.junit.runners.ParentRunner.run(ParentRunner.java:300)\n\tat org.junit.runners.Suite.runChild(Suite.java:128)\n\tat org.junit.runners.Suite.runChild(Suite.java:24)\n\tat org.junit.runners.ParentRunner$3.run(ParentRunner.java:231)\n\tat org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:60)\n\tat org.junit.runners.ParentRunner.runChildren(ParentRunner.java:229)\n\tat org.junit.runners.ParentRunner.access$000(ParentRunner.java:50)\n\tat org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:222)\n\tat org.junit.internal.runners.statements.RunAfters.evaluate(RunAfters.java:30)\n\tat org.junit.runners.ParentRunner.run(ParentRunner.java:300)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:157)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:136)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.execute(SlaveMain.java:114)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.main(SlaveMain.java:182)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMainSafe.main(SlaveMainSafe.java:6)\n",
      "assertionViolation": false,
      "assumptionViolation": false,
      "errorViolation": true
    },
    {
      "throwableClass": "java.lang.RuntimeException",
      "throwableString": "java.lang.RuntimeException",
      "stackTrace": "java.lang.RuntimeException\n\tat com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription.afterClass(TestHierarchicalSuiteDescription.java:46)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n\tat java.lang.reflect.Method.invoke(Method.java:597)\n\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:45)\n\tat org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)\n\tat org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:42)\n\tat org.junit.internal.runners.statements.RunAfters.evaluate(RunAfters.java:36)\n\tat org.junit.runners.ParentRunner.run(ParentRunner.java:300)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:157)\n\tat org.junit.runner.JUnitCore.run(JUnitCore.java:136)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.execute(SlaveMain.java:114)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMain.main(SlaveMain.java:182)\n\tat com.carrotsearch.ant.tasks.junit4.slave.SlaveMainSafe.main(SlaveMainSafe.java:6)\n",
      "assertionViolation": false,
      "assumptionViolation": false,
      "errorViolation": true
    }
  ],
  "output": [
    {
      "test": "method1(com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub2)",
      "event": "TEST_STARTED"
    },
    {
      "test": "method1(com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub2)",
      "event": "TEST_FINISHED"
    },
    {
      "test": "method1(com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub3)",
      "event": "TEST_STARTED"
    },
    {
      "test": "method1(com.carrotsearch.ant.tasks.junit4.tests.TestHierarchicalSuiteDescription$Sub3)",
      "event": "TEST_FINISHED"
    }
  ]
}

];

slaves = {
  "0": {
    "id": 0,
    "jvmName": "Java HotSpot(TM) 64-Bit Server VM, 16.3-b01",
    "charset": "UTF-8",
    "systemProperties": {
      "file.encoding": "UTF-8",
      "file.encoding.pkg": "sun.io",
      "file.separator": "/",
      "java.awt.graphicsenv": "sun.awt.X11GraphicsEnvironment",
      "java.awt.printerjob": "sun.print.PSPrinterJob",
      "java.class.path": "/home/dweiss/carrot2/carrotsearch.labs/randomizedtesting/ant-junit4/target/test-classes:/home/dweiss/carrot2/carrotsearch.labs/randomizedtesting/ant-junit4/target/dependency/junit-4.10.jar:/home/dweiss/carrot2/carrotsearch.labs/randomizedtesting/ant-junit4/target/classes",
      "java.class.version": "50.0",
      "java.endorsed.dirs": "/home/dweiss/Applications/java/jdk1.6.0_20/jre/lib/endorsed",
      "java.ext.dirs": "/home/dweiss/Applications/java/jdk1.6.0_20/jre/lib/ext:/usr/java/packages/lib/ext",
      "java.home": "/home/dweiss/Applications/java/jdk1.6.0_20/jre",
      "java.io.tmpdir": "/tmp",
      "java.library.path": "/home/dweiss/Applications/java/jdk1.6.0_20/jre/lib/amd64/server:/home/dweiss/Applications/java/jdk1.6.0_20/jre/lib/amd64:/home/dweiss/Applications/java/jdk1.6.0_20/jre/../lib/amd64:/usr/lib/jvm/java-6-openjdk/jre/lib/amd64/server:/usr/lib/jvm/java-6-openjdk/jre/lib/amd64:/usr/lib/jvm/java-6-openjdk/jre/../lib/amd64:/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib",
      "java.runtime.name": "Java(TM) SE Runtime Environment",
      "java.runtime.version": "1.6.0_20-b02",
      "java.specification.name": "Java Platform API Specification",
      "java.specification.vendor": "Sun Microsystems Inc.",
      "java.specification.version": "1.6",
      "java.vendor": "Sun Microsystems Inc.",
      "java.vendor.url": "http://java.sun.com/",
      "java.vendor.url.bug": "http://java.sun.com/cgi-bin/bugreport.cgi",
      "java.version": "1.6.0_20",
      "java.vm.info": "mixed mode",
      "java.vm.name": "Java HotSpot(TM) 64-Bit Server VM",
      "java.vm.specification.name": "Java Virtual Machine Specification",
      "java.vm.specification.vendor": "Sun Microsystems Inc.",
      "java.vm.specification.version": "1.0",
      "java.vm.vendor": "Sun Microsystems Inc.",
      "java.vm.version": "16.3-b01",
      "junit4.memory.total": "62259200",
      "junit4.processors": "2",
      "line.separator": "\n",
      "os.arch": "amd64",
      "os.name": "Linux",
      "os.version": "3.0.0-13-generic",
      "path.separator": ":",
      "sun.arch.data.model": "64",
      "sun.boot.class.path": "/home/dweiss/Applications/java/jdk1.6.0_20/jre/lib/resources.jar:/home/dweiss/Applications/java/jdk1.6.0_20/jre/lib/rt.jar:/home/dweiss/Applications/java/jdk1.6.0_20/jre/lib/sunrsasign.jar:/home/dweiss/Applications/java/jdk1.6.0_20/jre/lib/jsse.jar:/home/dweiss/Applications/java/jdk1.6.0_20/jre/lib/jce.jar:/home/dweiss/Applications/java/jdk1.6.0_20/jre/lib/charsets.jar:/home/dweiss/Applications/java/jdk1.6.0_20/jre/classes",
      "sun.boot.library.path": "/home/dweiss/Applications/java/jdk1.6.0_20/jre/lib/amd64",
      "sun.cpu.endian": "little",
      "sun.cpu.isalist": "",
      "sun.desktop": "gnome",
      "sun.io.unicode.encoding": "UnicodeLittle",
      "sun.java.launcher": "SUN_STANDARD",
      "sun.jnu.encoding": "UTF-8",
      "sun.management.compiler": "HotSpot 64-Bit Server Compiler",
      "sun.os.patch.level": "unknown",
      "user.country": "US",
      "user.dir": "/home/dweiss/carrot2/carrotsearch.labs/randomizedtesting/ant-junit4/target/test-classes",
      "user.home": "/home/dweiss",
      "user.language": "en",
      "user.name": "dweiss",
      "user.timezone": ""
    }
  }
};
