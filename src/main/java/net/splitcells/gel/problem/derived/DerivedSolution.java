package net.splitcells.gel.problem.derived;

import net.splitcells.dem.data.set.list.List;
import net.splitcells.dem.data.set.list.ListView;
import net.splitcells.dem.object.Discoverable;
import net.splitcells.gel.solution.Solution;
import net.splitcells.gel.solution.history.History;
import net.splitcells.gel.solution.history.Histories;
import net.splitcells.gel.data.database.AfterAdditionSubscriber;
import net.splitcells.gel.data.allocation.Allocations;
import net.splitcells.gel.data.table.Rinda;
import net.splitcells.gel.data.table.atribūts.Atribūts;
import net.splitcells.gel.data.table.kolonna.Kolonna;
import net.splitcells.gel.data.table.kolonna.KolonnaSkats;
import net.splitcells.gel.constraint.Ierobežojums;
import net.splitcells.gel.constraint.tips.Atvasināšana;
import net.splitcells.gel.data.database.Database;
import net.splitcells.gel.data.database.BeforeRemovalSubscriber;
import net.splitcells.gel.rating.structure.MetaRating;
import org.w3c.dom.Node;

import java.util.Set;
import java.util.function.Function;

import static net.splitcells.dem.utils.Not_implemented_yet.not_implemented_yet;


public class DerivedSolution implements Solution {

    protected Ierobežojums ierobežojums;
    private final Allocations piešķiršanas;
    private final History vēsture;
    private final Discoverable konteksts;
    
    public static DerivedSolution atvasinātaProblema(Discoverable konteksts, Allocations piešķiršanas, Ierobežojums originalIerobežojums, Function<MetaRating, MetaRating> atvasinātsFunkcija) {
        return new DerivedSolution(konteksts, piešķiršanas, originalIerobežojums, Atvasināšana.atvasināšana(originalIerobežojums, atvasinātsFunkcija));
    }

    protected DerivedSolution(Discoverable konteksts, Allocations piešķiršanas, Ierobežojums oriģinālaisIerobežojums, Function<MetaRating, MetaRating> atvasinātsFunkcija) {
        this(konteksts, piešķiršanas, oriģinālaisIerobežojums, Atvasināšana.atvasināšana(oriģinālaisIerobežojums, atvasinātsFunkcija));
    }

    public static DerivedSolution atvasinātsProblēma(Discoverable konteksts, Allocations piešķiršanas, Ierobežojums ierobežojums, Ierobežojums atvasināšana) {
        return new DerivedSolution(konteksts, piešķiršanas, ierobežojums, atvasināšana);
    }

    protected DerivedSolution(Discoverable konteksts, Allocations piešķiršanas, Ierobežojums ierobežojums, Ierobežojums atvasināšana) {
        this.piešķiršanas = piešķiršanas;
        this.ierobežojums = atvasināšana;
        vēsture = Histories.vēsture(this);
        this.konteksts = konteksts;
    }

    protected DerivedSolution(Discoverable konteksts, Allocations piešķiršanas) {
        this.piešķiršanas = piešķiršanas;
        vēsture = Histories.vēsture(this);
        this.konteksts = konteksts;
    }

    @Override
    public Ierobežojums ierobežojums() {
        return ierobežojums;
    }

    @Override
    public Allocations piešķiršanas() {
        return piešķiršanas;
    }

    @Override
    public Solution uzAtrisinājumu() {
        throw not_implemented_yet();
    }

    @Override
    public Solution kāAtrisinājums() {
        throw not_implemented_yet();
    }

    @Override
    public DerivedSolution atvasinājums(Function<MetaRating, MetaRating> atvasināšana) {
        throw not_implemented_yet();
    }

    @Override
    public Database piedāvājums() {
        return piešķiršanas.piedāvājums();
    }

    @Override
    public Database piedāvājumi_lietoti() {
        return piešķiršanas.piedāvājumi_lietoti();
    }

    @Override
    public Database piedāvājums_nelietots() {
        return piešķiršanas.piedāvājums_nelietots();
    }

    @Override
    public Database prasība() {
        return piešķiršanas.prasība();
    }

    @Override
    public Database prasība_lietots() {
        return piešķiršanas.prasība_lietots();
    }

    @Override
    public Database prasības_nelietotas() {
        return piešķiršanas.prasības_nelietotas();
    }

    @Override
    public Rinda piešķirt(Rinda prasība, Rinda piedāvājums) {
        return piešķiršanas.piešķirt(prasība, piedāvājums);
    }

    @Override
    public Rinda prasība_no_piešķiršana(Rinda piešķiršana) {
        return piešķiršanas.prasība_no_piešķiršana(piešķiršana);
    }

    @Override
    public Rinda piedāvājums_no_piešķiršana(Rinda piešķiršana) {
        return piešķiršanas.piedāvājums_no_piešķiršana(piešķiršana);
    }

    @Override
    public Set<Rinda> piešķiršanas_no_piedāvājuma(Rinda piedāvājums) {
        return piešķiršanas.piešķiršanas_no_piedāvājuma(piedāvājums);
    }

    @Override
    public Set<Rinda> piešķiršanas_no_prasības(Rinda prasība) {
        return piešķiršanas.piešķiršanas_no_prasības(prasība);
    }

    @Override
    public Rinda pieliktUnPārtulkot(List<?> vērtība) {
        return piešķiršanas.pieliktUnPārtulkot(vērtība);
    }

    @Override
    public Rinda pielikt(Rinda rinda) {
        return piešķiršanas.pielikt(rinda);
    }

    @Override
    public void noņemt(int lineIndex) {
        piešķiršanas.noņemt(lineIndex);
    }

    @Override
    public void noņemt(Rinda rinda) {
        piešķiršanas.noņemt(rinda);
    }

    @Override
    public void abonē_uz_papildinājums(AfterAdditionSubscriber klausītājs) {
        piešķiršanas.abonē_uz_papildinājums(klausītājs);
    }

    @Override
    public void abonē_uz_iepriekšNoņemšana(BeforeRemovalSubscriber pirmsNoņemšanasKlausītājs) {
        piešķiršanas.abonē_uz_iepriekšNoņemšana(pirmsNoņemšanasKlausītājs);
    }

    @Override
    public void abonē_uz_pēcNoņemšana(BeforeRemovalSubscriber klausītājs) {
        piešķiršanas.abonē_uz_pēcNoņemšana(klausītājs);
    }

    @Override
    public List<Atribūts<Object>> nosaukumuSkats() {
        return piešķiršanas.nosaukumuSkats();
    }

    @Override
    public <T> KolonnaSkats<T> kolonnaSkats(Atribūts<T> atribūts) {
        return piešķiršanas.kolonnaSkats(atribūts);
    }

    @Override
    public List<Kolonna<Object>> kolonnaSkats() {
        return piešķiršanas.kolonnaSkats();
    }

    @Override
    public ListView<Rinda> jēlaRindasSkats() {
        return piešķiršanas.jēlaRindasSkats();
    }

    @Override
    public int izmērs() {
        return piešķiršanas.izmērs();
    }

    @Override
    public List<Rinda> jēlasRindas() {
        return piešķiršanas.jēlasRindas();
    }

    @Override
    public Rinda uzmeklēVienādus(Atribūts<Rinda> atribūts, Rinda other) {
        return piešķiršanas.uzmeklēVienādus(atribūts, other);
    }

    @Override
    public Node toDom() {
        throw not_implemented_yet();
    }

    @Override
    public net.splitcells.dem.data.set.list.List<String> path() {
        return konteksts.path().withAppended(DerivedSolution.class.getSimpleName());
    }

    @Override
    public History vēsture() {
        return vēsture;
    }
}
