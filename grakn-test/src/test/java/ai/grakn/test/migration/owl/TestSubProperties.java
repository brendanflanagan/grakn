/*
 * Grakn - A Distributed Semantic Database
 * Copyright (C) 2016  Grakn Labs Limited
 *
 * Grakn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Grakn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Grakn. If not, see <http://www.gnu.org/licenses/gpl.txt>.
 */

package ai.grakn.test.migration.owl;

import ai.grakn.exception.GraknValidationException;
import ai.grakn.graql.Graql;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Map;
import java.util.Set;

public class TestSubProperties extends TestOwlGraknBase {
	private IRI baseIri = IRI.create("http://www.workingontologist.org/Examples/Chapter3/shakespeare.owl");
	private OWLOntology shakespeare = null;
	
	@Before
	public void loadShakespeare() throws GraknValidationException {
        shakespeare = loadOntologyFromResource("owl", "shakespeare.owl");
        migrator.ontology(shakespeare).graph(graph).migrate();
        migrator.graph().commit();
	}
	
    @Test
    public void testSubpropertyInference() {
    	Reasoner reasoner = new Reasoner(new Configuration(), shakespeare);
    	IRI createdProp = baseIri.resolve("#created");
    	Map<OWLNamedIndividual, Set<OWLNamedIndividual>> createdInstances = 
    			reasoner.getObjectPropertyInstances(manager.getOWLDataFactory().getOWLObjectProperty(createdProp));
    	int owlCount = createdInstances.values().stream().mapToInt(S -> S.size()).sum();
        int mmCount = migrator.graph().graql().match(Graql.var("r").isa(migrator.namer().objectPropertyName(createdProp)))
    		.stream().mapToInt(M -> 1).sum();
    	Assert.assertEquals(owlCount, mmCount);
    }
}