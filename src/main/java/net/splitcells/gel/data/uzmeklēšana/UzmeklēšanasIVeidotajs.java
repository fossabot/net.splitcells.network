package net.splitcells.gel.data.uzmeklēšana;

import net.splitcells.gel.data.table.Tabula;
import net.splitcells.gel.data.table.atribūts.Atribūts;

public class UzmeklēšanasIVeidotajs implements UzmeklēšanasVeidotajs {
    @Override
    public <R> Uzmeklēšana<R> uzmeklē(Tabula tabula, Atribūts<R> atribūts) {
        return new UzmeklēšanaI<>(tabula, atribūts);
    }

    @Override
    public void close() {

    }

    @Override
    public void flush() {

    }
}
