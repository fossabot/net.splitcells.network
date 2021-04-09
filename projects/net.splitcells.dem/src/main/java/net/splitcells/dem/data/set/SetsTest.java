package net.splitcells.dem.data.set;

import net.splitcells.dem.data.atom.BoolI;
import net.splitcells.dem.data.set.list.Lists;
import net.splitcells.dem.environment.config.IsDeterministic;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static net.splitcells.dem.Dem.configValue;
import static net.splitcells.dem.Dem.process;
import static net.splitcells.dem.data.set.list.Lists.list;
import static org.assertj.core.api.Assertions.assertThat;

public class SetsTest {
    @Test
    public void testForDeterministicFactory() {
        assertThat
                (process(() -> assertThat(configValue(Sets.class).set()._isDeterministic()).contains(true)
                        , env -> env.config().withConfigValue(IsDeterministic.class, Optional.of(BoolI.truthful()))
                ).hasError())
                .isFalse();
        assertThat
                (process(() -> assertThat(configValue(Sets.class).set(list())._isDeterministic()).contains(true)
                        , env -> env.config().withConfigValue(IsDeterministic.class, Optional.of(BoolI.truthful()))
                ).hasError())
                .isFalse();
    }
}