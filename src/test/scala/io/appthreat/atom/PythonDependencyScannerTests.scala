package io.appthreat.atom

import io.appthreat.atom.parsedeps.{ModuleWithVersion, PythonDependencyParser}
import io.appthreat.pysrc2cpg.PySrc2CpgFixture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.File

class PythonDependencyScannerTests extends PySrc2CpgFixture(withOssDataflow = false) {

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
        ModuleWithVersion("certifi", "", ">=2017.4.17", ""),
        ModuleWithVersion("charset_normalizer", "", ">=2,<4", ""),
        ModuleWithVersion("dbt-core", "", "~=1.7,<1.8", ""),
        ModuleWithVersion("google-api-core[grpc]", "", ">= 1.34.0, <3.0.0dev,!=2.0.*,!=2.1.*,!=2.2.*,!=2.3.*,!=2.4.*,!=2.5.*,!=2.6.*,!=2.7.*,!=2.8.*,!=2.9.*,!=2.10.*", ""),
        ModuleWithVersion("idna", "", ">=2.5,<4", ""),
        ModuleWithVersion("os", "", "", "os.path"),
        ModuleWithVersion("packageA", "", ">=1.4.2,<1.9,!=1.5.*,!=1.6.*", ""),
        ModuleWithVersion("packageB", "", ">=0.5.0,< 0.7.0", ""),
        ModuleWithVersion("proto-plus", "", ">= 1.22.2, <2.0.0dev; python_version>='3.11',>= 1.22.0, <2.0.0dev", ""),
        ModuleWithVersion("protobuf", "", ">=3.19.5,<5.0.0dev,!=3.20.0,!=3.20.1,!=4.21.0,!=4.21.1,!=4.21.2,!=4.21.3,!=4.21.4,!=4.21.5", ""),
        ModuleWithVersion("re-wx", "", ">=0.0.2", ""),
        ModuleWithVersion("socket", "", "", "socket"),
        ModuleWithVersion("typing-extensions", "3.10.0.2", "", ""),
        ModuleWithVersion("urllib3", "", ">=1.21.1,<3", "urllib3.poolmanager.proxy_from_url,urllib3.util.Timeout,urllib3.exceptions.LocationValueError,urllib3.contrib.socks.SOCKSProxyManager,urllib3.exceptions.HTTPError,urllib3.exceptions.SSLError,urllib3.exceptions.ProxyError,urllib3.exceptions.InvalidHeader,urllib3.exceptions.MaxRetryError,urllib3.exceptions.ConnectTimeoutError,urllib3.exceptions.ClosedPoolError,urllib3.exceptions.ProtocolError,urllib3.util.retry.Retry,urllib3.exceptions.ResponseError,urllib3.exceptions.ReadTimeoutError,urllib3.exceptions.NewConnectionError,urllib3.util.parse_url,urllib3.poolmanager.PoolManager"),
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

}
