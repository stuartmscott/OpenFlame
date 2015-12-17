/*
 * The OpenFlame Project <http://stuartmscott.github.io/OpenFlame/>.
 *
 * Copyright (C) 2015 OpenFlame Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package expression.function;

import expression.IExpression;
import expression.Literal;
import main.IMatch;
import main.ITarget;
import main.Utilities;

import java.util.Map;

public class FlameAssemble extends Function {

    private static final String MAIN = "main";
    private static final String OUT_DIRECTORY = "./out/flame";
    private static final String MKDIR_COMMAND = "mkdir -p out/flame";
    private static final String ASSEMBLE_COMMAND = "java -jar %s -m %s -b %s %s";

    private String mName;
	private IExpression mMain;
	private IExpression mSource;
	private String mBinary;

	public FlameAssemble(IMatch match, ITarget target, Map<String, IExpression> parameters) {
        super(match, target, parameters);
        IExpression name = getParameter(NAME);
        if (!(name instanceof Literal)) {
            mMatch.error("FlameAssemble function expects a String name");
        }
        mName = name.resolve();
        mMain = getParameter(MAIN);
        mSource = getParameter(SOURCE);
        mBinary = String.format("%s/%s", OUT_DIRECTORY, mName);
    }

	/**
     * {@inheritDoc}
     */
    @Override
    public void configure() {
        mMatch.addFile(mBinary);
        mMain.configure();
        mSource.configure();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve() {
        String assembler = mMatch.getProperty("assembler");
        mMatch.awaitFile(assembler);// Wait for Assembler
        String files = Utilities.join(" ", mSource.resolveList());
        mMatch.runCommand(MKDIR_COMMAND);
        mMatch.runCommand(String.format(ASSEMBLE_COMMAND, assembler, mMain.resolve(), mBinary, files));
        mMatch.provideFile(mBinary);
        return mBinary;
    }
}