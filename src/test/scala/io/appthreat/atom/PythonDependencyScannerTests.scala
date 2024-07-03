package io.appthreat.atom

import io.appthreat.atom.parsedeps.{ModuleWithVersion, PythonDependencyParser}
import io.appthreat.pysrc2cpg.PySrc2CpgFixture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.File

class PythonDependencyScannerTests extends PySrc2CpgFixture(withOssDataflow = false):

    "dependencies from the `requests` library" should {
        lazy val cpg = code(
          """
        |#!/usr/bin/env python
        |import os
        |import sys
        |from codecs import open
        |
        |from setuptools import setup
        |from setuptools.command.test import test as TestCommand
        |
        |
        |
        |class PyTest(TestCommand):
        |    user_options = [("pytest-args=", "a", "Arguments to pass into py.test")]
        |
        |    def initialize_options(self):
        |        TestCommand.initialize_options(self)
        |        try:
        |            from multiprocessing import cpu_count
        |
        |            self.pytest_args = ["-n", str(cpu_count()), "--boxed"]
        |        except (ImportError, NotImplementedError):
        |            self.pytest_args = ["-n", "1", "--boxed"]
        |
        |    def finalize_options(self):
        |        TestCommand.finalize_options(self)
        |        self.test_args = []
        |        self.test_suite = True
        |
        |    def run_tests(self):
        |        import pytest
        |
        |        errno = pytest.main(self.pytest_args)
        |        sys.exit(errno)
        |
        |
        |# 'setup.py publish' shortcut.
        |if sys.argv[-1] == "publish":
        |    os.system("python setup.py sdist bdist_wheel")
        |    os.system("twine upload dist/*")
        |    sys.exit()
        |
        |requires = [
        |    "charset_normalizer>=2,<4",
        |    "idna>=2.5,<4",
        |    "urllib3>=1.21.1,<3",
        |    "certifi>=2017.4.17",
        |    "packageA>=1.4.2,<1.9,!=1.5.*,!=1.6.*",
        |    "packageB>=0.5.0,< 0.7.0",
        |    "PickyThing<1.6,>1.9,!=1.9.6,<2.0a0,==2.4c1",
        |    "PackageC==1.2.0.dev1+hg.5.b11e5e6f0b0b",
        |    "typing-extensions==3.10.0.2",
        |    "re-wx>=0.0.2",
        |    "zope.interface>=5.1.0",
        |    "google-api-core[grpc] >= 1.34.0, <3.0.0dev,!=2.0.*,!=2.1.*,!=2.2.*,!=2.3.*,!=2.4.*,!=2.5.*,!=2.6.*,!=2.7.*,!=2.8.*,!=2.9.*,!=2.10.*",
        |    "proto-plus >= 1.22.0, <2.0.0dev",
        |    "proto-plus >= 1.22.2, <2.0.0dev; python_version>='3.11'",
        |    "protobuf>=3.19.5,<5.0.0dev,!=3.20.0,!=3.20.1,!=4.21.0,!=4.21.1,!=4.21.2,!=4.21.3,!=4.21.4,!=4.21.5",
        |    "dbt-core~=1.7,<1.8"
        |]
        |test_requirements = [
        |    "pytest-httpbin==2.0.0",
        |    "pytest-cov",
        |    "pytest-mock",
        |    "pytest-xdist",
        |    "PySocks>=1.5.6, !=1.5.7",
        |    "pytest>=3",
        |]
        |
        |about = {}
        |here = os.path.abspath(os.path.dirname(__file__))
        |with open(os.path.join(here, "requests", "__version__.py"), "r", "utf-8") as f:
        |    exec(f.read(), about)
        |
        |with open("README.md", "r", "utf-8") as f:
        |    readme = f.read()
        |
        |setup(
        |    name=about["__title__"],
        |    version=about["__version__"],
        |    description=about["__description__"],
        |    long_description=readme,
        |    long_description_content_type="text/markdown",
        |    author=about["__author__"],
        |    author_email=about["__author_email__"],
        |    url=about["__url__"],
        |    packages=["requests"],
        |    package_data={"": ["LICENSE", "NOTICE"]},
        |    package_dir={"requests": "requests"},
        |    include_package_data=True,
        |    python_requires=">=3.7",
        |    install_requires=requires,
        |    license=about["__license__"],
        |    zip_safe=False,
        |    classifiers=[
        |        "Development Status :: 5 - Production/Stable",
        |        "Environment :: Web Environment",
        |        "Intended Audience :: Developers",
        |        "License :: OSI Approved :: Apache Software License",
        |        "Natural Language :: English",
        |        "Operating System :: OS Independent",
        |        "Programming Language :: Python",
        |        "Programming Language :: Python :: 3",
        |        "Programming Language :: Python :: 3.7",
        |        "Programming Language :: Python :: 3.8",
        |        "Programming Language :: Python :: 3.9",
        |        "Programming Language :: Python :: 3.10",
        |        "Programming Language :: Python :: 3.11",
        |        "Programming Language :: Python :: 3 :: Only",
        |        "Programming Language :: Python :: Implementation :: CPython",
        |        "Programming Language :: Python :: Implementation :: PyPy",
        |        "Topic :: Internet :: WWW/HTTP",
        |        "Topic :: Software Development :: Libraries",
        |    ],
        |    cmdclass={"test": PyTest},
        |    tests_require=test_requirements,
        |    extras_require={
        |        "security": [],
        |        "socks": ["PySocks>=1.5.6, !=1.5.7"],
        |        "use_chardet_on_py3": ["chardet>=3.0.2,<6"],
        |    },
        |    project_urls={
        |        "Documentation": "https://requests.readthedocs.io",
        |        "Source": "https://github.com/psf/requests",
        |    },
        |)
        |
        |""".stripMargin,
          "setup.py"
        ).moreCode(
          """
        |import os.path
        |import socket  # noqa: F401
        |
        |from urllib3.exceptions import ClosedPoolError, ConnectTimeoutError
        |from urllib3.exceptions import HTTPError as _HTTPError
        |from urllib3.exceptions import InvalidHeader as _InvalidHeader
        |from urllib3.exceptions import (
        |    LocationValueError,
        |    MaxRetryError,
        |    NewConnectionError,
        |    ProtocolError,
        |)
        |from urllib3.exceptions import ProxyError as _ProxyError
        |from urllib3.exceptions import ReadTimeoutError, ResponseError
        |from urllib3.exceptions import SSLError as _SSLError
        |from urllib3.poolmanager import PoolManager, proxy_from_url
        |from urllib3.util import Timeout as TimeoutSauce
        |from urllib3.util import parse_url
        |from urllib3.util.retry import Retry
        |
        |from .auth import _basic_auth_str
        |from .compat import basestring, urlparse
        |from .cookies import extract_cookies_to_jar
        |from .exceptions import (
        |    ConnectionError,
        |    ConnectTimeout,
        |    InvalidHeader,
        |    InvalidProxyURL,
        |    InvalidSchema,
        |    InvalidURL,
        |    ProxyError,
        |    ReadTimeout,
        |    RetryError,
        |    SSLError,
        |)
        |from .models import Response
        |from .structures import CaseInsensitiveDict
        |from .utils import (
        |    DEFAULT_CA_BUNDLE_PATH,
        |    extract_zipped_paths,
        |    get_auth_from_url,
        |    get_encoding_from_headers,
        |    prepend_scheme_if_needed,
        |    select_proxy,
        |    urldefragauth,
        |)
        |
        |try:
        |    from urllib3.contrib.socks import SOCKSProxyManager
        |except ImportError:
        |
        |    def SOCKSProxyManager(*args, **kwargs):
        |        raise InvalidSchema("Missing dependencies for SOCKS support.")
        |
        |""".stripMargin,
          Seq("requests", "adapters.py").mkString(File.separator)
        )

        "have the modules scanned successfully" in {
            val scanResult = PythonDependencyParser.parse(cpg)
            scanResult.modules shouldBe List(
              ModuleWithVersion("PackageC", "1.2.0.dev1+hg.5.b11e5e6f0b0b", "", ""),
              ModuleWithVersion("PickyThing", "2.4c1", "<1.6,>1.9,!=1.9.6,<2.0a0", ""),
              ModuleWithVersion("PySocks", "", ">=1.5.6, !=1.5.7", ""),
              ModuleWithVersion("certifi", "", ">=2017.4.17", ""),
              ModuleWithVersion("charset_normalizer", "", ">=2,<4", ""),
              ModuleWithVersion("dbt-core", "", "~=1.7,<1.8", ""),
              ModuleWithVersion(
                "google-api-core[grpc]",
                "",
                ">= 1.34.0, <3.0.0dev,!=2.0.*,!=2.1.*,!=2.2.*,!=2.3.*,!=2.4.*,!=2.5.*,!=2.6.*,!=2.7.*,!=2.8.*,!=2.9.*,!=2.10.*",
                ""
              ),
              ModuleWithVersion("idna", "", ">=2.5,<4", ""),
              ModuleWithVersion("os", "", "", "os.path"),
              ModuleWithVersion("packageA", "", ">=1.4.2,<1.9,!=1.5.*,!=1.6.*", ""),
              ModuleWithVersion("packageB", "", ">=0.5.0,< 0.7.0", ""),
              ModuleWithVersion(
                "proto-plus",
                "",
                ">= 1.22.2, <2.0.0dev; python_version>='3.11',>= 1.22.0, <2.0.0dev",
                ""
              ),
              ModuleWithVersion(
                "protobuf",
                "",
                ">=3.19.5,<5.0.0dev,!=3.20.0,!=3.20.1,!=4.21.0,!=4.21.1,!=4.21.2,!=4.21.3,!=4.21.4,!=4.21.5",
                ""
              ),
              ModuleWithVersion("pytest", "", ">=3", ""),
              ModuleWithVersion("pytest-cov", "", "", ""),
              ModuleWithVersion("pytest-httpbin", "2.0.0", "", ""),
              ModuleWithVersion("pytest-mock", "", "", ""),
              ModuleWithVersion("pytest-xdist", "", "", ""),
              ModuleWithVersion("re-wx", "", ">=0.0.2", ""),
              ModuleWithVersion("socket", "", "", "socket"),
              ModuleWithVersion("typing-extensions", "3.10.0.2", "", ""),
              ModuleWithVersion(
                "urllib3",
                "",
                ">=1.21.1,<3",
                "urllib3.poolmanager.proxy_from_url,urllib3.util.Timeout,urllib3.exceptions.LocationValueError,urllib3.contrib.socks.SOCKSProxyManager,urllib3.exceptions.HTTPError,urllib3.exceptions.SSLError,urllib3.exceptions.ProxyError,urllib3.exceptions.InvalidHeader,urllib3.exceptions.MaxRetryError,urllib3.exceptions.ConnectTimeoutError,urllib3.exceptions.ClosedPoolError,urllib3.exceptions.ProtocolError,urllib3.util.retry.Retry,urllib3.exceptions.ResponseError,urllib3.exceptions.ReadTimeoutError,urllib3.exceptions.NewConnectionError,urllib3.util.parse_url,urllib3.poolmanager.PoolManager"
              ),
              ModuleWithVersion("zope.interface", "", ">=5.1.0", "")
            )
        }
    }

    "dependencies from the `impacket` library" should {
        lazy val cpg = code(
          """
              |#!/usr/bin/env python
              |# $Id$
              |
              |import glob
              |import os
              |import platform
              |
              |from setuptools import setup
              |
              |PACKAGE_NAME = "impacket2"
              |
              |if platform.system() != 'Darwin':
              |    data_files = [(os.path.join('share', 'doc', PACKAGE_NAME), ['README.md', 'LICENSE']+glob.glob('doc/*'))]
              |else:
              |    data_files = []
              |
              |def read(fname):
              |    return open(os.path.join(os.path.dirname(__file__), fname)).read()
              |
              |setup(name = PACKAGE_NAME,
              |      version = "0.9.21-dev",
              |      package_dir={'': 'src'},
              |      platforms = ["Unix"],
              |      packages=['impacket2', 'impacket2.dcerpc', 'impacket2.examples', 'impacket2.dcerpc.v5', 'impacket2.dcerpc.v5.dcom',
              |                'impacket2.krb5', 'impacket2.ldap', 'impacket2.examples.ntlmrelayx',
              |                'impacket2.examples.ntlmrelayx.clients', 'impacket2.examples.ntlmrelayx.servers',
              |                'impacket2.examples.ntlmrelayx.servers.socksplugins', 'impacket2.examples.ntlmrelayx.utils',
              |                'impacket2.examples.ntlmrelayx.attacks'],
              |      data_files = data_files,
              |      install_requires=['pyasn1>=0.2.3', 'pycryptodomex', 'pyOpenSSL>=0.13.1', 'six', 'ldap3==2.5.1', 'ldapdomaindump>=0.9.0', 'flask>=1.0'],
              |      extras_require={
              |                      'pyreadline:sys_platform=="win32"': [],
              |                      'python_version<"2.7"': [ 'argparse' ],
              |                    },
              |      classifiers = [
              |          "Programming Language :: Python :: 3.6",
              |          "Programming Language :: Python :: 2.7",
              |          "Programming Language :: Python :: 2.6",
              |      ]
              |)
              |
              |""".stripMargin,
          "setup.py"
        )

        "have the modules scanned successfully" in {
            val scanResult = PythonDependencyParser.parse(cpg)
            scanResult.modules shouldBe List(
                ModuleWithVersion("flask", "", ">=1.0", ""),
                ModuleWithVersion("ldap3", "2.5.1", "", ""),
                ModuleWithVersion("ldapdomaindump", "", ">=0.9.0", ""),
                ModuleWithVersion("pyOpenSSL", "", ">=0.13.1", ""),
                ModuleWithVersion("pyasn1", "", ">=0.2.3", ""),
                ModuleWithVersion("pycryptodomex", "", "", ""),
                ModuleWithVersion("six", "", "", "")
            )
        }
    }

    "dependencies from the `evadb` project" should {
        lazy val cpg = code(
            """
              |import io
              |import os
              |
              |# to read contents of README file
              |from pathlib import Path
              |from typing import Dict
              |
              |from setuptools import find_packages, setup
              |from setuptools.command.install import install
              |from subprocess import check_call
              |
              |this_directory = Path(__file__).parent
              |LONG_DESCRIPTION = (this_directory / "README.md").read_text()
              |
              |DESCRIPTION = "EvaDB AI-Relational Database System"
              |NAME = "evadb"
              |AUTHOR = "Georgia Tech Database Group"
              |AUTHOR_EMAIL = "arulraj@gatech.edu"
              |URL = "https://github.com/georgia-tech-db/eva"
              |
              |# Check Python version
              |# import sys
              |# if sys.version_info < (3, 8):
              |#     sys.exit("Python 3.8 or later is required.")
              |
              |
              |def read(path, encoding="utf-8"):
              |    path = os.path.join(os.path.dirname(__file__), path)
              |    with io.open(path, encoding=encoding) as fp:
              |        return fp.read()
              |
              |
              |# version.py defines the VERSION and VERSION_SHORT variables
              |VERSION_DICT: Dict[str, str] = {}
              |with open("evadb/version.py", "r") as version_file:
              |    exec(version_file.read(), VERSION_DICT)
              |
              |DOWNLOAD_URL = "https://github.com/georgia-tech-db/eva"
              |LICENSE = "Apache License 2.0"
              |VERSION = VERSION_DICT["VERSION"]
              |
              |minimal_requirements = [
              |    "numpy>=1.19.5",
              |    "pandas>=2.1.0", # DataFrame.map is available after v2.1.0
              |    "sqlalchemy>=2.0.0",
              |    "sqlalchemy-utils>=0.36.6",
              |    "lark>=1.0.0",
              |    "pyyaml>=5.1",
              |    "aenum>=2.2.0",
              |    "diskcache>=5.4.0",
              |    "retry>=0.9.2",
              |    "pydantic<2",  # ray-project/ray#37019.
              |    "psutil",
              |    "thefuzz",
              |]
              |
              |vision_libs = [
              |    "torch>=1.10.0",
              |    "torchvision>=0.11.1",
              |    "transformers",  # HUGGINGFACE
              |    "faiss-cpu",  # DEFAULT VECTOR INDEX
              |    "opencv-python-headless>=4.6.0.66",
              |    "Pillow>=8.4.0",
              |    "eva-decord>=0.6.1",  # VIDEO PROCESSING
              |    "ultralytics>=8.0.93",  # OBJECT DETECTION
              |    "timm>=0.6.13",  # HUGGINGFACE VISION TASKS
              |    "sentencepiece",  # TRANSFORMERS
              |]
              |
              |document_libs = [
              |    "transformers",  # HUGGINGFACE
              |    "langchain",  # DATA LOADERS
              |    "faiss-cpu",  # DEFAULT VECTOR INDEX
              |    "pymupdf<1.23.0",  # pymupdf/PyMuPDF#2617 and pymupdf/PyMuPDF#2614
              |    "pdfminer.six",
              |    "sentence-transformers",
              |    "protobuf",
              |    "bs4",
              |    "openai>=1.0",  # CHATGPT
              |    "gpt4all",  # PRIVATE GPT
              |    "sentencepiece",  # TRANSFORMERS
              |]
              |
              |function_libs = [
              |    "facenet-pytorch>=2.5.2",  # FACE DETECTION
              |    "pytube",  # YOUTUBE QA APP
              |    "youtube-transcript-api",  # YOUTUBE QA APP
              |    "boto3",  # AWS
              |    "norfair>=2.2.0",  # OBJECT TRACKING
              |    "kornia",  # SIFT FEATURES
              |]
              |
              |ray_libs = [
              |    "ray>=1.13.0,<2.5.0",  # BREAKING CHANGES IN 2.5.0
              |]
              |
              |notebook_libs = [
              |    "ipython<8.13.0",
              |    "ipywidgets>=7.7.2",
              |    "matplotlib>=3.3.4",
              |    "nbmake>=1.2.1",
              |    "nest-asyncio>=1.5.6",
              |]
              |
              |qdrant_libs = ["qdrant_client"]  # cannot install on 3.11 due to grcpio
              |
              |pinecone_libs = ["pinecone-client"]
              |
              |chromadb_libs = ["chromadb"]
              |
              |weaviate_libs = ["weaviate-client"]
              |
              |milvus_libs = ["pymilvus>=2.3.0"]
              |
              |
              |postgres_libs = [
              |    "psycopg2",
              |]
              |
              |ludwig_libs = ["ludwig[hyperopt,distributed]"]  # MODEL TRAIN AND FINE TUNING
              |
              |sklearn_libs = ["scikit-learn"]
              |
              |xgboost_libs = ["flaml[automl]"]
              |
              |hackernews_libs = ["requests"]
              |
              |forecasting_libs = [
              |    "statsforecast",  # MODEL TRAIN AND FINE TUNING
              |    "neuralforecast",  # MODEL TRAIN AND FINE TUNING
              |]
              |
              |imagegen_libs = [
              |    "replicate"
              |]
              |
              |### NEEDED FOR DEVELOPER TESTING ONLY
              |
              |dev_libs = [
              |    # TESTING PACKAGES
              |    "pytest>=6.1.2",
              |    "pytest-cov>=2.11.1",
              |    "mock",
              |    "coveralls>=3.0.1",
              |    "moto[s3]>=4.1.1",
              |    "pytest-testmon",
              |    # BENCHMARK PACKAGES
              |    "pytest-benchmark",
              |    # LINTING PACKAGES
              |    "codespell",
              |    "pylint",
              |    "black>=23.1.0",
              |    "isort>=5.10.1",
              |    "flake8>=3.9.1",
              |    # DISTRIBUTION PACKAGES
              |    "wheel>=0.37.1",
              |    "semantic_version",
              |    "PyGithub",
              |    "twine",
              |    "PyDriller",
              |]
              |
              |INSTALL_REQUIRES = minimal_requirements
              |
              |EXTRA_REQUIRES = {
              |    "ray": ray_libs,
              |    "vision": vision_libs,
              |    "document": document_libs,
              |    "function": function_libs,
              |    "notebook": notebook_libs,
              |    "qdrant": qdrant_libs,
              |    "pinecone": pinecone_libs,
              |    "chromadb": chromadb_libs,
              |    "milvus": milvus_libs,
              |    "weaviate": weaviate_libs,
              |    "postgres": postgres_libs,
              |    "ludwig": ludwig_libs,
              |    "sklearn": sklearn_libs,
              |    "xgboost": xgboost_libs,
              |    "forecasting": forecasting_libs,
              |    "hackernews": hackernews_libs,
              |    # everything except ray, qdrant, ludwig and postgres. The first three fail on pyhton 3.11.
              |    "dev": dev_libs + vision_libs + document_libs + function_libs + notebook_libs + forecasting_libs + sklearn_libs + imagegen_libs + xgboost_libs
              |}
              |
              |setup(
              |    name=NAME,
              |    version=VERSION,
              |    description=DESCRIPTION,
              |    long_description=LONG_DESCRIPTION,
              |    long_description_content_type="text/markdown",
              |    author=AUTHOR,
              |    author_email=AUTHOR_EMAIL,
              |    url=URL,
              |    download_url=DOWNLOAD_URL,
              |    license=LICENSE,
              |    classifiers=[
              |        "Development Status :: 5 - Production/Stable",
              |        "License :: OSI Approved :: Apache Software License",
              |        "Programming Language :: Python :: 3.8",
              |        "Programming Language :: Python :: 3.9",
              |        "Programming Language :: Python :: 3.10",
              |        # "Programming Language :: Python :: 3.11",
              |    ],
              |    packages=find_packages(exclude=["tests", "tests.*"]),
              |    # https://python-packaging.readthedocs.io/en/latest/command-line-scripts.html#the-console-scripts-entry-point
              |    entry_points={
              |        "console_scripts": [
              |            "evadb_server=evadb.evadb_server:main",
              |            "evadb_client=evadb.evadb_cmd_client:main",
              |        ]
              |    },
              |    python_requires=">=3.8",
              |    install_requires=INSTALL_REQUIRES,
              |    extras_require=EXTRA_REQUIRES,
              |    include_package_data=True,
              |    package_data={
              |        "evadb": [
              |            "evadb.yml",
              |            "parser/evadb.lark",
              |            "third_party/databases/**/requirements.txt",
              |        ]
              |    },
              |)
              |
              |""".stripMargin,
            "setup.py"
        )

        "have the modules scanned successfully" in {
            val scanResult = PythonDependencyParser.parse(cpg)
            scanResult.modules shouldBe List(
                ModuleWithVersion("Pillow", "", ">=8.4.0", ""),
                ModuleWithVersion("PyDriller", "", "", ""),
                ModuleWithVersion("PyGithub", "", "", ""),
                ModuleWithVersion("aenum", "", ">=2.2.0", ""),
                ModuleWithVersion("black", "", ">=23.1.0", ""),
                ModuleWithVersion("boto3", "", "", ""),
                ModuleWithVersion("bs4", "", "", ""),
                ModuleWithVersion("chromadb", "", "", ""),
                ModuleWithVersion("codespell", "", "", ""),
                ModuleWithVersion("coveralls", "", ">=3.0.1", ""),
                ModuleWithVersion("diskcache", "", ">=5.4.0", ""),
                ModuleWithVersion("eva-decord", "", ">=0.6.1", ""),
                ModuleWithVersion("facenet-pytorch", "", ">=2.5.2", ""),
                ModuleWithVersion("faiss-cpu", "", "", ""),
                ModuleWithVersion("flake8", "", ">=3.9.1", ""),
                ModuleWithVersion("flaml[automl]", "", "", ""),
                ModuleWithVersion("gpt4all", "", "", ""),
                ModuleWithVersion("ipython", "", "<8.13.0", ""),
                ModuleWithVersion("ipywidgets", "", ">=7.7.2", ""),
                ModuleWithVersion("isort", "", ">=5.10.1", ""),
                ModuleWithVersion("kornia", "", "", ""),
                ModuleWithVersion("langchain", "", "", ""),
                ModuleWithVersion("lark", "", ">=1.0.0", ""),
                ModuleWithVersion("ludwig[hyperopt,distributed]", "", "", ""),
                ModuleWithVersion("matplotlib", "", ">=3.3.4", ""),
                ModuleWithVersion("mock", "", "", ""),
                ModuleWithVersion("moto[s3]", "", ">=4.1.1", ""),
                ModuleWithVersion("nbmake", "", ">=1.2.1", ""),
                ModuleWithVersion("nest-asyncio", "", ">=1.5.6", ""),
                ModuleWithVersion("neuralforecast", "", "", ""),
                ModuleWithVersion("norfair", "", ">=2.2.0", ""),
                ModuleWithVersion("numpy", "", ">=1.19.5", ""),
                ModuleWithVersion("openai", "", ">=1.0", ""),
                ModuleWithVersion("opencv-python-headless", "", ">=4.6.0.66", ""),
                ModuleWithVersion("pandas", "", ">=2.1.0", ""),
                ModuleWithVersion("pdfminer.six", "", "", ""),
                ModuleWithVersion("pinecone-client", "", "", ""),
                ModuleWithVersion("protobuf", "", "", ""),
                ModuleWithVersion("psutil", "", "", ""),
                ModuleWithVersion("psycopg2", "", "", ""),
                ModuleWithVersion("pydantic", "", "<2", ""),
                ModuleWithVersion("pylint", "", "", ""),
                ModuleWithVersion("pymilvus", "", ">=2.3.0", ""),
                ModuleWithVersion("pymupdf", "", "<1.23.0", ""),
                ModuleWithVersion("pytest", "", ">=6.1.2", ""),
                ModuleWithVersion("pytest-benchmark", "", "", ""),
                ModuleWithVersion("pytest-cov", "", ">=2.11.1", ""),
                ModuleWithVersion("pytest-testmon", "", "", ""),
                ModuleWithVersion("pytube", "", "", ""),
                ModuleWithVersion("pyyaml", "", ">=5.1", ""),
                ModuleWithVersion("qdrant_client", "", "", ""),
                ModuleWithVersion("ray", "", ">=1.13.0,<2.5.0", ""),
                ModuleWithVersion("replicate", "", "", ""),
                ModuleWithVersion("requests", "", "", ""),
                ModuleWithVersion("retry", "", ">=0.9.2", ""),
                ModuleWithVersion("scikit-learn", "", "", ""),
                ModuleWithVersion("semantic_version", "", "", ""),
                ModuleWithVersion("sentence-transformers", "", "", ""),
                ModuleWithVersion("sentencepiece", "", "", ""),
                ModuleWithVersion("sqlalchemy", "", ">=2.0.0", ""),
                ModuleWithVersion("sqlalchemy-utils", "", ">=0.36.6", ""),
                ModuleWithVersion("statsforecast", "", "", ""),
                ModuleWithVersion("thefuzz", "", "", ""),
                ModuleWithVersion("timm", "", ">=0.6.13", ""),
                ModuleWithVersion("torch", "", ">=1.10.0", ""),
                ModuleWithVersion("torchvision", "", ">=0.11.1", ""),
                ModuleWithVersion("transformers", "", "", ""),
                ModuleWithVersion("twine", "", "", ""),
                ModuleWithVersion("ultralytics", "", ">=8.0.93", ""),
                ModuleWithVersion("weaviate-client", "", "", ""),
                ModuleWithVersion("wheel", "", ">=0.37.1", ""),
                ModuleWithVersion("youtube-transcript-api", "", "", "")
            )
        }
    }
end PythonDependencyScannerTests
