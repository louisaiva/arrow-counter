#!/usr/bin/env python3
"""
Arrow Counter Mod Build Script
Compiles Java sources and packages them with assets into a JAR file.
"""

import os
import sys
import subprocess
import shutil
import json
from pathlib import Path

# Configuration
PROJECT_ROOT = Path(__file__).parent.resolve()
HYTALE_SERVER_JAR = PROJECT_ROOT.parent / "base code" / "Server" / "HytaleServer.jar"
CODE_DIR = PROJECT_ROOT / "code"
MANIFEST_FILE = PROJECT_ROOT / "manifest.json"
BIN_DIR = PROJECT_ROOT / "bin"
BUILD_DIR = PROJECT_ROOT / "build"

# Read version from manifest
def get_mod_info():
    """Extract mod name and version from manifest.json"""
    with open(MANIFEST_FILE, 'r') as f:
        manifest = json.load(f)
    return manifest.get("Name", "ArrowCounter"), manifest.get("version", "1.0.0")

def clean():
    """Clean build artifacts"""
    print("[*] Cleaning previous builds...")
    if BIN_DIR.exists():
        shutil.rmtree(BIN_DIR)
    print("    ✓ Cleaned bin/")

def compile_java():
    """Compile Java source files"""
    print("[*] Compiling Java sources...")
    
    if not HYTALE_SERVER_JAR.exists():
        print(f"    ✗ Error: HytaleServer.jar not found at {HYTALE_SERVER_JAR}")
        return False
    
    # Find all .java files in code/ directory
    java_files = list(CODE_DIR.glob("*.java"))
    if not java_files:
        print("    ✗ Error: No Java files found in code/")
        return False
    
    print(f"    Found {len(java_files)} Java file(s)")
    
    # Create bin directory
    BIN_DIR.mkdir(exist_ok=True)
    
    # Compile with javac - store in package structure
    cmd = [
        "javac",
        f"-cp", str(HYTALE_SERVER_JAR),
        "-d", str(BIN_DIR),
        *[str(f) for f in java_files]
    ]
    
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, check=False)
        
        # Print warnings/errors
        if result.stderr:
            print("    Compiler output:", result.stderr.strip())
        
        if result.returncode != 0:
            print("    ✗ Compilation failed")
            return False
        
        print("    ✓ Compilation successful")
        return True
    
    except FileNotFoundError:
        print("    ✗ Error: javac not found. Make sure Java is installed and in PATH")
        return False

def package_jar(mod_name, version):
    """Package compiled classes and assets into JAR directly to build/ folder"""
    print(f"[*] Packaging JAR file...")
    
    # Create temporary staging directory
    staging_dir = BUILD_DIR / "_staging"
    if staging_dir.exists():
        shutil.rmtree(staging_dir)
    staging_dir.mkdir(parents=True, exist_ok=True)
    
    # Copy compiled classes to nomaj/arrowcounter structure
    if BIN_DIR.exists():
        for file in BIN_DIR.rglob("*.class"):
            rel_path = file.relative_to(BIN_DIR)
            dest_path = staging_dir / "nomaj" / "arrowcounter" / file.name
            dest_path.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(file, dest_path)
    
    # Create Common/UI/Custom/Pages directory and copy UI files and icons
    common_pages = staging_dir / "Common" / "UI" / "Custom" / "Pages"
    common_pages.mkdir(parents=True, exist_ok=True)
    
    # Copy .ui and .png files from code/ to Common/UI/Custom/Pages/
    for file in CODE_DIR.glob("*.ui"):
        shutil.copy2(file, common_pages / file.name)
    for file in CODE_DIR.glob("*.png"):
        shutil.copy2(file, common_pages / file.name)
    
    # Copy manifest.json
    shutil.copy2(MANIFEST_FILE, staging_dir / "manifest.json")
    
    # Create JAR file directly in build/ folder
    jar_name = f"{mod_name.lower()}_{version}.jar"
    jar_path = BUILD_DIR / jar_name
    
    try:
        # Change to staging directory and create jar
        cwd = os.getcwd()
        os.chdir(staging_dir)
        
        # Get all files to add to jar
        files_to_jar = []
        for root, dirs, files in os.walk("."):
            for file in files:
                files_to_jar.append(os.path.join(root, file))
        
        # Create jar using jar command
        cmd = ["jar", "cf", str(jar_path)] + files_to_jar
        result = subprocess.run(cmd, capture_output=True, text=True, check=False)
        
        os.chdir(cwd)
        
        # Clean up staging directory
        shutil.rmtree(staging_dir)
        
        if result.returncode != 0:
            print(f"    ✗ JAR creation failed: {result.stderr}")
            return False
        
        print(f"    ✓ Created JAR: {jar_path}")
        return True
    
    except FileNotFoundError:
        print("    ✗ Error: jar command not found. Make sure Java is installed and in PATH")
        return False
    except Exception as e:
        print(f"    ✗ Error creating JAR: {e}")
        return False

def main():
    """Main build process"""
    print("=" * 60)
    print("Arrow Counter Mod - Build Script")
    print("=" * 60)
    
    # Get mod info
    mod_name, version = get_mod_info()
    print(f"\nBuilding {mod_name} v{version}")
    print(f"Project root: {PROJECT_ROOT}\n")
    
    # Check dependencies
    if not MANIFEST_FILE.exists():
        print("✗ Error: manifest.json not found")
        return False
    
    # Run build steps
    clean()
    
    if not compile_java():
        return False
    
    if not package_jar(mod_name, version):
        return False
    
    print("\n" + "=" * 60)
    print("✓ Build completed successfully!")
    print("=" * 60)
    return True

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
