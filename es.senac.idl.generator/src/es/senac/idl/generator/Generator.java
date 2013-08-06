package es.senac.idl.generator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.csu.cpp.output.CppBeautifier;
import org.csu.idl.idlmm.TranslationUnit;
import org.csu.idl.preprocessor.Preprocessor;
import org.csu.idl.xtext.loader.IDLLoader;
import org.eclipse.internal.xpand2.pr.ProtectedRegionResolverImpl;
import org.eclipse.xpand2.XpandExecutionContextImpl;
import org.eclipse.xpand2.XpandFacade;
import org.eclipse.xpand2.output.Outlet;
import org.eclipse.xpand2.output.OutputImpl;
import org.eclipse.xtend.expression.Variable;
import org.eclipse.xtext.conversion.impl.AbstractDeclarativeValueConverterService;

public class Generator {

	private Map<String, Variable> globalVarsMap = new HashMap<String, Variable>();

	private IDLLoader loader = new IDLLoader();

	private Preprocessor preprocessor = null;

	public Generator() {
		preprocessor = loader.getPreprocessor();
	}

	public void run(String[] args) {

		proccessArgs(args);
		
		try {
			loader.load(filePath);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		TranslationUnit model = loader.getModel();

		// Xpand
		generate(model);
	}

	private void generate(TranslationUnit model) {
		// http://www.peterfriese.de/using-xpand-in-your-eclipse-wizards/

		// Configure outlets
		OutputImpl output = new OutputImpl();
		Outlet outlet = new Outlet("model");
		outlet.setOverwrite(true);
		// outlet.addPostprocessor(new CppBeautifier());
		outlet.setPath(targetDir);
		output.addOutlet(outlet);

		// Protected regions
		ProtectedRegionResolverImpl pr = new ProtectedRegionResolverImpl();
		pr.setSrcPathes(prSrcPaths);
		pr.setDefaultExcludes(true);
		pr.setIgnoreList("*.swp");

		// Execution context
		XpandExecutionContextImpl execCtx = new XpandExecutionContextImpl(output, pr, globalVarsMap, null, null);
		execCtx.registerMetaModel(new org.eclipse.xtend.type.impl.java.JavaBeansMetaModel());

		// Generate transport classes
		XpandFacade facade = XpandFacade.create(execCtx);
		final String templatePath = "templates::Main::main";
		facade.evaluate(templatePath, model);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Generator().run(args);
	}

	//
	//
	// Command line options
	// http://snippets.dzone.com/posts/show/3504
	//

	@SuppressWarnings("unchecked")
	private void proccessArgs(String[] args) {
		CommandLineParser parser = new GnuParser();
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println("Error parsing arguments");
			System.exit(1);
		}

		if (cmd.hasOption("h")) {
			System.out.println("Options:");
			for (Option opt_ : (Collection<Option>) options.getOptions()) {
				System.out.println("-" + opt_.getOpt() + "\t" + opt_.getDescription());
			}
			System.exit(0);
		}

		for (Option opt : cmd.getOptions()) {
			if (opt.getOpt() == ("o"))
				targetDir = opt.getValue();

			// Preproccessor options

			if (opt.getOpt() == ("D"))
				preprocessor.addMacroDefinition(opt.getValue());

			if (opt.getOpt() == ("U"))
				preprocessor.undefMacro(opt.getValue());

			if (opt.getOpt() == ("I"))
				preprocessor.addIncludePath(opt.getValue());

		}

		globalVarsMap.put("targetDir", new Variable("targetDir", targetDir));

		// Verbose
		if (!cmd.hasOption("v")) {
			// Solo errores
			System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
			Logger.getRootLogger().setLevel(Level.ERROR);
			Logger.getLogger(AbstractDeclarativeValueConverterService.class).setLevel(Level.ERROR);
		}

		// IDL file
		if (cmd.getArgList().size() == 1)
			filePath = (String) cmd.getArgList().get(0);
		else {
			System.err.println("No IDL file specified or more than one.");
			System.exit(1);
		}
	}

	private static Options options = null; // Command line options

	private CommandLine cmd = null; // Command Line arguments

	// By default
	private String targetDir = ".";
	private String prSrcPaths = "";
	private String filePath = null;

	static {
		options = new Options();

		// options.addOption(opt, hasArg, description);
		// options.addOption(opt, longOpt, hasArg, description);

		options.addOption("v", false, "Verbose.");

		options.addOption("o", true, "Output directory for the generated files. Default is current directory.");

//		options.addOption("E", false, "Only invoke the preprocessor.");
		options.addOption("h", false, "Show this help text.");

		// Preproccessor options
		options.addOption("D", true, "Passed to the preprocessor.");
		options.addOption("U", true, "Passed to the preprocessor.");
		options.addOption("I", true, "Passed to the preprocessor.");

	}
}
