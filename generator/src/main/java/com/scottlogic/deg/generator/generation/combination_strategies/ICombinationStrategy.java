package com.scottlogic.deg.generator.generation.combination_strategies;

import com.scottlogic.deg.generator.generation.databags.DataBag;

import java.util.List;

public interface ICombinationStrategy {
    Iterable<DataBag> permute(List<Iterable<DataBag>> dataBagSequences);
}