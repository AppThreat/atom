import argparse
import os
from zipfile import ZipFile

from atomgen.lib import atom

os.environ["PYTHONIOENCODING"] = "utf-8"
os.environ["PYTHONUTF8"] = "1"


def build_args():
    """
    Constructs command line arguments for the scanner
    """
    parser = argparse.ArgumentParser(description="Atom generator")
    parser.add_argument(
        "-i",
        "--src",
        dest="src",
        help="Source directory or file",
        default=os.getcwd(),
    )
    parser.add_argument(
        "-o",
        "--out-dir",
        dest="atom_out_dir",
        help="Atom output directory",
        default=os.getcwd(),
    )
    return parser.parse_args()


def main():
    """Main method"""
    args = build_args()

    methodFullName = atom.CpgStructNodeProperty(
        name=atom.NodePropertyName.FULL_NAME, value=atom.PropertyValue("main")
    )

    method = atom.CpgStructNode(
        key=1, type=atom.NodeType.METHOD, property=[methodFullName]
    )

    atom_struct = atom.CpgStruct(node=[method])
    os.makedirs(args.atom_out_dir, exist_ok=True)
    persist(atom_struct, file_name=os.path.join(args.atom_out_dir, "app.atom"))


def persist(atom_struct, file_name="app.atom"):
    """persist atom (of type CpgStruct) to disk, e.g. so you can open it in joern:
    joern> importCpg("/path/to/app.atom")
    """
    with ZipFile(file_name, "w") as zip_file:
        zip_file.writestr("cpg.proto", bytes(atom_struct))
    print(file_name, "created successfully")


if __name__ == "__main__":
    main()
