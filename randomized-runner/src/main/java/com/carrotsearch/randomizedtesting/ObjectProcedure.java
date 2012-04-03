package com.carrotsearch.randomizedtesting;

interface ObjectProcedure<KType>
{
    public void apply(KType value);
}
